package org.shijinglu.asrc.gateway;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.shijinglu.asrc.core.Formula;
import org.shijinglu.asrc.core.IFormulaProvider;
import org.yaml.snakeyaml.Yaml;

/** Provide formulas from local yaml files (possibly synced with a git repository). */
public class YamlFormulaProvider implements IFormulaProvider {
    private final Path rootDir;
    private ImmutableMap<String, Map<String, Formula>> allFormulas = null;
    private ImmutableMap<String, Set<String>> allKeys = null;

    public YamlFormulaProvider(Path rootDir) throws IOException {
        this.rootDir = rootDir;
        loadYamlFiles(rootDir);
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

    static class ParseYamlVisitor extends SimpleFileVisitor<Path> {
        private final ImmutableMap.Builder<String, Map<String, Formula>> nsFormulasBuilder =
                ImmutableMap.builder();

        private final ImmutableMap.Builder<String, Set<String>> nsKeysBuilder =
                ImmutableMap.builder();
        private final Path root;

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
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            List<Object> rawFormulas =
                    new Yaml().load(new BufferedInputStream(new FileInputStream(file.toFile())));

            ImmutableMap.Builder<String, Formula> formulasBuilder = ImmutableMap.builder();
            ImmutableSet.Builder<String> keysBuilder = ImmutableSet.builder();

            String namespace = pathToNamespace(root, file);
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
            Set<String> keys = keysBuilder.build();
            if (!keys.isEmpty()) {
                nsFormulasBuilder.put(namespace, formulasBuilder.build());
                nsKeysBuilder.put(namespace, keysBuilder.build());
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private synchronized void loadYamlFiles(Path path) throws IOException {
        ParseYamlVisitor visitor = new ParseYamlVisitor(rootDir);
        Files.walkFileTree(path, visitor);
        this.allFormulas = visitor.getFormulas();
        this.allKeys = visitor.getKeys();
    }

    @Override
    public Map<String, Set<String>> allKeys() {
        return this.allKeys;
    }

    @Override
    public Optional<Formula> getFormula(String namespace, String key) {
        return Optional.ofNullable(this.allFormulas.get(namespace))
                .flatMap(x -> Optional.ofNullable(x.get(key)));
    }
}
