package org.shijinglu.asrc.core;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.shijinglu.lure.LureException;
import org.shijinglu.lure.extensions.IData;
import org.shijinglu.lure.extensions.IFunction;

/**
 * An UDF in lure that can Look up values from KVStore e.g. {@code cache("hello") == "world" } first
 * looks up value in the KVStrore and then compare that value to "world"
 *
 * <p>e.g. {@code cache("username", "alice") == "alexander hamilton"}
 */
public class UdfMap implements IFunction {
    private final KVStore kvStore;

    public UdfMap(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public String functionName() {
        return "cache";
    }

    @Override
    public IData derive(List<IData> list) throws LureException {
        if (list.size() == 1) {
            return kvStore.get(list.get(0).toString(), String.class).orElse(null);
        } else if (list.size() == 2) {
            return kvStore.get(list.get(0).toString(), list.get(1).toString(), String.class)
                    .orElse(null);
        }
        Logger.getLogger(UdfMap.class.getName())
                .log(Level.WARNING, "failed to derive data for " + list);
        return null;
    }
}
