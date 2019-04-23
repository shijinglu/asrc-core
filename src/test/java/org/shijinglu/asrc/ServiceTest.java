package org.shijinglu.asrc;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ServiceTest {

    @Test
    public void test_me() {
        String s = "TOOLS-1234 ";
        ImmutableList.Builder<String> resBuilder = ImmutableList.builder();


        List<String> list = Splitter.on(",").splitToList(s).stream().map(String::trim).collect(Collectors.toList());
        System.out.printf("res = %s", list);
    }

}