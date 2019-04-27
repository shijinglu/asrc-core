package org.shijinglu.asrc;

import com.google.common.base.Splitter;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class ServiceTest {

    @Test
    public void test_me() {
        String s = "TOOLS-1234 ";
        List<String> list =
                Splitter.on(",").splitToList(s).stream()
                        .map(String::trim)
                        .collect(Collectors.toList());
        System.out.printf("res = %s", list);
        URL url = ServiceTest.class.getResource("/formula_fixture1.yaml");
        System.out.println(url);
    }

    @Test
    public void test_reflection() {
        Integer abc = 123;
        Assert.assertEquals(abc.getClass(), Integer.class);
        // assert abc.getClass() ;
    }
}
