package org.shijinglu.asrc.core;

import java.nio.file.Paths;
import java.util.Collections;
import org.shijinglu.asrc.gateway.IJedisProvider;
import org.shijinglu.asrc.gateway.RedisStore;
import org.shijinglu.asrc.gateway.SingleRedisProvider;
import org.shijinglu.asrc.gateway.YamlFormulaProvider;
import org.shijinglu.lure.extensions.IFunction;

/**
 * This class demonstrates how to create a configuration _service with following plugins: 1. A user
 * defined function (UDF) that can resolve strings to strings. a.k.a redis backend KV store. 2.
 * Formulas provided from local YAML files 3. Forwarding event to console print
 */
public class ExampleServiceModule {
    private final String _namespace;
    private final Service _service;

    public ExampleServiceModule(String formulaPath) {
        IJedisProvider jedisProvider = new SingleRedisProvider();
        KVStore store = new RedisStore(jedisProvider);
        IFunction udf = new UdfMap(store);
        YamlFormulaProvider formulaProvider = new YamlFormulaProvider(Paths.get(formulaPath));
        String[] namespaces = formulaProvider.allKeys().keySet().toArray(new String[0]);
        _namespace = namespaces[0];
        _service = new Service(formulaProvider, Collections.singletonList(udf), new EventSender());
    }

    public String getNamespace() {
        return _namespace;
    }

    public Service getService() {
        return _service;
    }
}
