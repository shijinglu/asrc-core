package org.shijinglu.asrc.gateway;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class YamlFormulaProviderTest {
    private static final String GIT_REPO_URL = "git@github.com:shijinglu/asrc-yaml-examples.git";

    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException, InterruptedException {
        String[] cmd = {"git", "clone", GIT_REPO_URL, folder.getRoot().getAbsolutePath()};
        int code = Runtime.getRuntime().exec(cmd, null, folder.getRoot()).waitFor();
        System.out.println(String.join(" ", cmd) + " -> " + code);
    }

    @Test
    public void testLoading() throws IOException {
        Path yamlFolder = Paths.get(folder.getRoot().getAbsolutePath(), "main");
        YamlFormulaProvider provider = new YamlFormulaProvider(yamlFolder);
        Assert.assertNotNull(provider.allKeys());
        Assert.assertNotNull(provider.getFormula("com_example_services_xyz", "db-url"));
    }
}
