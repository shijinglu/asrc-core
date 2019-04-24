package org.shijinglu.asrc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.shijinglu.asrc.model.Formula;
import org.yaml.snakeyaml.Yaml;

public class ServiceTest {

    @Test
    public void test_me() {
        String s = "TOOLS-1234 ";
        ImmutableList.Builder<String> resBuilder = ImmutableList.builder();

        List<String> list =
                Splitter.on(",").splitToList(s).stream()
                        .map(String::trim)
                        .collect(Collectors.toList());
        System.out.printf("res = %s", list);
        URL url = ServiceTest.class.getResource("/formula_fixture1.yaml");
        System.out.println(url);
    }

    public static class ToyFormula {
        String category;
    }

    public static class ToyFormulas {
        // List<ToyFormula> formulas;
        public String firstName;
    }

    @Test
    public void test_yaml() throws IOException {
        YAMLFactory factory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        String yaml = "firstName: Bob\n";
        ToyFormulas formulas = mapper.readValue(yaml, ToyFormulas.class);
        Assert.assertNotNull(formulas);
    }

    @Test
    public void testSnakeYaml() {
        Yaml yaml = new Yaml();
        List<Object> dict =
                yaml.load(ServiceTest.class.getResourceAsStream("/formula_fixture1.yaml"));
        System.out.println(dict);
        Assert.assertEquals(dict.size(), 2);
        Optional<Formula> f1 = Formula.parse((Map) dict.get(1));

        Assert.assertTrue(f1.isPresent());
        Optional<Formula> f0 = Formula.parse((Map) dict.get(0));
        Assert.assertTrue(f0.isPresent());
    }
}
