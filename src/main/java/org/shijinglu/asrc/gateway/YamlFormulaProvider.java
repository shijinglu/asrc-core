package org.shijinglu.asrc.gateway;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.shijinglu.asrc.core.Formula;
import org.shijinglu.asrc.core.IFormula;
import org.shijinglu.asrc.core.IFormulaProvider;
import org.yaml.snakeyaml.Yaml;

/**
 * Provide formulas from local yaml files (possibly synced with a git repository). This class
 * promise not to throw any exception for loading failures. Instead, it logs the IOExceptions to
 * {@code java.util.logging} facility.
 */
public class YamlFormulaProvider implements IFormulaProvider {
    private final Path rootDir;
    private ImmutableMap<String, Map<String, Formula>> allFormulas = null;
    private ImmutableMap<String, Set<String>> allKeys = null;

    /**
     * Load formulas from a directory or a single YAML file.
     *
     * @param rootDir
     */
    public YamlFormulaProvider(Path rootDir) {
        this.rootDir = rootDir;
        loadYamlFiles(rootDir);
    }

    /**
     * Load formulas from a file stream, given its namespace.
     *
     * @param namespace
     * @param yamlFileStream
     */
    public YamlFormulaProvider(String namespace, InputStream yamlFileStream) {

        rootDir = Paths.get("/");
        Map.Entry<Map<String, Formula>, Set<String>> parsed = loadYaml(yamlFileStream);
        allFormulas = ImmutableMap.of(namespace, parsed.getKey());
        allKeys = ImmutableMap.of(namespace, parsed.getValue());
    }

    private static String pathToNamespace(Path root, Path file) {
        Path relative = root.relativize(file);
        List<String> names =
                Streams.stream(relative.iterator())
                        .map(
                                path -> {
                                    if (Files.isRegularFile(root.resolve(path))) {
                                        String filename = path.getFileName().toString();
                                        int extensionIndex = filename.lastIndexOf(".");
                                        if (extensionIndex == -1) return filename;
                                        return filename.substring(0, extensionIndex);
                                    }
                                    return path.getFileName().toString();
                                })
                        .collect(Collectors.toList());
        return String.join("_", names);
    }

    private static Map.Entry<Map<String, Formula>, Set<String>> loadYaml(InputStream inputStream) {
        List<Object> rawFormulas = new Yaml().load(new BufferedInputStream(inputStream));

        ImmutableMap.Builder<String, Formula> formulasBuilder = ImmutableMap.builder();
        ImmutableSet.Builder<String> keysBuilder = ImmutableSet.builder();
        rawFormulas.forEach(
                obj -> {
                    if (obj instanceof Map) {
                        Optional<Formula> maybeFormula = Formula.parse((Map) obj);
                        maybeFormula.ifPresent(
                                f -> {
                                    formulasBuilder.put(f.getKey(), f);
                                    keysBuilder.add(f.getKey());
                                });
                    }
                });
        return new AbstractMap.SimpleEntry<>(formulasBuilder.build(), keysBuilder.build());
    }

    static class ParseYamlVisitor extends SimpleFileVisitor<Path> {
        private final ImmutableMap.Builder<String, Map<String, Formula>> nsFormulasBuilder =
                ImmutableMap.builder();
        private final ImmutableMap.Builder<String, Set<String>> nsKeysBuilder =
                ImmutableMap.builder();

        private final Path root; // save root to compute namespace

        ParseYamlVisitor(Path root) {
            this.root = root;
        }

        ImmutableMap<String, Map<String, Formula>> getFormulas() {
            return nsFormulasBuilder.build();
        }

        ImmutableMap<String, Set<String>> getKeys() {
            return nsKeysBuilder.build();
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

            InputStream inputStream;
            try {
                inputStream = new FileInputStream(file.toFile());
            } catch (IOException e) {
                Logger.getLogger(YamlFormulaProvider.class.getName())
                        .log(Level.WARNING, "Yaml file not exist", e);
                return FileVisitResult.CONTINUE;
            }
            String namespace = pathToNamespace(root, file);
            Map.Entry<Map<String, Formula>, Set<String>> ret = loadYaml(inputStream);
            if (!ret.getValue().isEmpty()) {
                nsFormulasBuilder.put(namespace, ret.getKey());
                nsKeysBuilder.put(namespace, ret.getValue());
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private synchronized void loadYamlFiles(Path path) {
        ParseYamlVisitor visitor;
        if (Files.isDirectory(path)) {
            visitor = new ParseYamlVisitor(rootDir);
            try {
                Files.walkFileTree(path, visitor);
            } catch (IOException e) {
                Logger.getLogger(YamlFormulaProvider.class.getName())
                        .log(Level.WARNING, "could not load at least one formula", e);
            }
        } else {
            visitor = new ParseYamlVisitor(rootDir.getParent());
            visitor.visitFile(path, null);
        }
        this.allFormulas = visitor.getFormulas();
        this.allKeys = visitor.getKeys();
    }

    @Override
    public Map<String, Set<String>> allKeys() {
        return this.allKeys;
    }

    @Override
    public Optional<IFormula> getFormula(String namespace, String key) {
        return Optional.ofNullable(this.allFormulas.get(namespace))
                .flatMap(x -> Optional.ofNullable(x.get(key)));
    }
}
