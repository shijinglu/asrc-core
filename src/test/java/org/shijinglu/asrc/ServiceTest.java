package org.shijinglu.asrc;

import static junit.framework.TestCase.assertEquals;

import com.google.common.base.Splitter;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.junit.Test;
import org.shijinglu.asrc.core.EventHandler;
import org.shijinglu.asrc.core.EventSender;
import org.shijinglu.asrc.core.IEvent;
import org.shijinglu.asrc.core.Service;
import org.shijinglu.asrc.gateway.YamlFormulaProvider;
import org.shijinglu.lure.Expr;
import org.shijinglu.lure.core.BoolData;
import org.shijinglu.lure.core.DoubleData;
import org.shijinglu.lure.core.IntData;
import org.shijinglu.lure.core.StringData;
import org.shijinglu.lure.extensions.IData;

public class ServiceTest {

    private static final Map<String, IData> EXAMPLE_CONTEXT =
            new Expr.ContextBuilder()
                    .addBoolContext("toggle_flag_on", true)
                    .addBoolContext("toggle_flag_off", false)
                    .addDoubleContext("PI", 3.14)
                    .addDoubleContext("NATURAL_CONSTANT_E", 2.718)
                    .addIntContext("NY_ZIP", 10001)
                    .addIntContext("SF_ZIP", 94142)
                    .addStringContext("first_name", "Alice")
                    .addStringContext("last_name", "Liddell")
                    .build();

    private Service getService(String namespace, InputStream yamlFileStream) {
        YamlFormulaProvider formulaProvider = new YamlFormulaProvider(namespace, yamlFileStream);
        return new Service(formulaProvider, Collections.emptyList(), new EventSender());
    }

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
    public void testOverrides() {
        String namespace = "overrides";
        Service service =
                getService(namespace, ServiceTest.class.getResourceAsStream("/overrides.yaml"));
        Map<String, IData> res = service.getConfigs(namespace, EXAMPLE_CONTEXT);
        assertEquals(res.get("toggle_flag_on"), BoolData.FALSE);
        assertEquals(res.get("toggle_flag_off"), BoolData.TRUE);
        assertEquals(res.get("PI"), new DoubleData(3.1415926));
        assertEquals(res.get("NATURAL_CONSTANT_E"), new DoubleData(2.71828));
        assertEquals(res.get("NY_ZIP"), new IntData(10002));
        assertEquals(res.get("SF_ZIP"), new IntData(94597));
        assertEquals(res.get("first_name"), new StringData("Harry"));
        assertEquals(res.get("last_name"), new StringData("Potter"));

        assertEquals(res.get("additional_bool"), BoolData.TRUE);
        assertEquals(res.get("additional_int"), new IntData(123));
        assertEquals(res.get("additional_double"), new DoubleData(0.123));
        assertEquals(res.get("additional_string"), new StringData("hello world!"));
    }

    @Test
    public void testSearch() {
        String namespace = "search";
        Service service =
                getService(namespace, ServiceTest.class.getResourceAsStream("/search.yaml"));
        Map<String, IData> res = service.getConfigs(namespace, EXAMPLE_CONTEXT);
        assertEquals(res.get("config_000"), new IntData(1000));
        assertEquals(res.get("config_001"), new IntData(1001));
        assertEquals(res.get("config_01"), new IntData(101));
        assertEquals(res.get("config_10"), new IntData(110));
        assertEquals(res.get("config_11"), new IntData(111));
    }

    @Test
    public void testFallThrough() {
        String namespace = "fallthrough";
        Service service =
                getService(namespace, ServiceTest.class.getResourceAsStream("/fallthrough.yaml"));
        Map<String, IData> res = service.getConfigs(namespace, EXAMPLE_CONTEXT);
        assertEquals(res.get("config_000"), new IntData(1000));
        assertEquals(res.get("config_001"), new IntData(1001));
        assertEquals(res.get("config_01"), new IntData(101));
        assertEquals(res.get("config_10"), new IntData(110));
        assertEquals(res.get("config_11"), new IntData(111));
    }

    @Test
    public void testAction() {
        String namespace = "actions";
        Service service =
                getService(namespace, ServiceTest.class.getResourceAsStream("/actions.yaml"));

        ConcurrentLinkedQueue<IEvent> sentEvents = new ConcurrentLinkedQueue<>();
        EventSender sender =
                new EventSender() {
                    @Override
                    public boolean send(IEvent event) {
                        sentEvents.add(event);
                        return true;
                    }
                };
        EventHandler.setSender(sender);
        service.getConfigs(namespace, EXAMPLE_CONTEXT);
        // FIXME: this test is flaky because it does not wait for all completable futures to drain
        assertEquals(sentEvents.size(), 11);
        EventHandler.setSender(null);
    }
}
