package org.shijinglu.asrc.core;

import java.util.Optional;
import org.shijinglu.lure.core.BoolData;
import org.shijinglu.lure.core.DoubleData;
import org.shijinglu.lure.core.IntData;
import org.shijinglu.lure.core.StringData;
import org.shijinglu.lure.extensions.IData;

/**
 * Interface to a key value store. Implementation of this can be Redis, RedisCluster or memcached
 */
public abstract class KVStore {
    protected abstract Optional<String> get(String key);

    protected abstract Optional<String> get(String namespace, String key);

    Optional<IData> get(String key, Class clazz) {
        return get(key).map(x -> KVStore.castTo(x, clazz));
    }

    Optional<IData> get(String namespace, String key, Class clazz) {
        return get(namespace, key).map(x -> KVStore.castTo(x, clazz));
    }

    static IData castTo(String val, Class clazz) {
        if (!clazz.isPrimitive()) {
            return new StringData(val);
        }
        if (clazz == Boolean.class) {
            return BoolData.fromString(val);
        }
        if (clazz == Integer.class || clazz == Short.class) {
            return IntData.fromString(val);
        }
        if (clazz == Long.class || clazz == Float.class || clazz == Double.class) {
            return DoubleData.fromString(val);
        }
        return new StringData(val);
    }
}
