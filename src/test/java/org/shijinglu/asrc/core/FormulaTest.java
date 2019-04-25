package org.shijinglu.asrc.core;

import static org.shijinglu.asrc.core.Formula.Category.SEGMENT;
import static org.shijinglu.asrc.core.Formula.Category.TREATMENT;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class FormulaTest {
    @Test
    public void testSnakeYaml() {
        Yaml yaml = new Yaml();
        List<Object> dict =
                yaml.load(FormulaTest.class.getResourceAsStream("/formula_fixture1.yaml"));
        System.out.println(dict);
        Assert.assertEquals(dict.size(), 2);

        Formula f0 = Formula.parse((Map) dict.get(0)).get();
        Assert.assertEquals(f0.key, "db-url");
        Assert.assertEquals(f0.data.get().toString(), "localhost");

        Formula f1 = Formula.parse((Map) dict.get(1)).get();
        Assert.assertEquals(f1.key, "sigma_threshold");

        Formula f10 = f1.formulas.get(0);
        Assert.assertEquals(f10.category, SEGMENT);
        Assert.assertEquals(f10.key, "high tech companies");
        Assert.assertEquals(f10.rule.toString(), "stock_id in (314, 98, 299979, 6022)");
        Assert.assertFalse(f10.fallthrough);

        Formula f100 = f10.formulas.get(0);
        Assert.assertEquals(f100.category, TREATMENT);
        Assert.assertEquals(f100.key, "control");
        Assert.assertEquals(f100.rule.toString(), "md5mod($APP_NAME, 100) <= 50");
        Assert.assertEquals(f100.data.get().toInt(), 100);

        Formula f111 = f1.formulas.get(1).formulas.get(1);
        Assert.assertEquals(f111.category, TREATMENT);
        Assert.assertEquals(f111.key, "treate");
        Assert.assertEquals(f111.rule.toString(), "true");
        Assert.assertFalse(f111.fallthrough);
        Assert.assertEquals(f111.data.get().toInt(), 111);
    }
}
