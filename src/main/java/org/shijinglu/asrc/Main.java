package org.shijinglu.asrc;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.shijinglu.asrc.core.ExampleServiceModule;
import org.shijinglu.lure.core.StringData;
import org.shijinglu.lure.extensions.IData;

public class Main {

    private static void print(Map<String, IData> configs) {
        configs.forEach(
                (k, v) -> {
                    System.out.println(k + " -> " + v.toString());
                });
    }

    public static void main(String[] args) throws ArgumentParserException {
        ArgumentParser parser =
                ArgumentParsers.newArgumentParser("validate")
                        .description(
                                "Validate and run the remote configure server for a single formula file");
        parser.addArgument("-f", "--formula").required(true).help("path to a formula file");

        Namespace ns = parser.parseArgs(args);

        ExampleServiceModule module = new ExampleServiceModule(ns.getString("formula"));

        Properties properties = System.getProperties();
        HashMap<String, IData> input = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            input.put(key, new StringData(properties.getProperty(key)));
        }
        Map<String, IData> output = module.getService().getConfigs(module.getNamespace(), input);

        System.out.println("----------- INPUT ------------");
        print(input);
        System.out.println("----------- OUTPUT ------------");
        print(output);
    }
}
