package org.shijinglu.asrc.core;

import com.google.common.annotations.VisibleForTesting;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import org.shijinglu.lure.extensions.ExtensionManager;
import org.shijinglu.lure.extensions.IData;
import org.shijinglu.lure.extensions.IFunction;

public class Service {
    private final IFormulaProvider formulaProvider;
    private static final int PARALLELISM = 100;
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(PARALLELISM);
    /**
     * Construct the service.
     *
     * @param formulaProvider
     * @param udfs user defined functions. e.g. `user_id("alice") in [1, 2, 3]` e.g.
     *     `volatility(stock_name) > 0.4`
     */
    public Service(
            IFormulaProvider formulaProvider, List<IFunction> udfs, EventSender eventSender) {
        this.formulaProvider = formulaProvider;
        udfs.forEach(ExtensionManager::addFunction);
        if (eventSender != null) {
            EventHandler.setSender(eventSender);
        }
    }

    /**
     * Calculate the remote config data given key and context.
     *
     * @return null if no matching formula is found or no corresponding formula at all
     */
    @VisibleForTesting
    protected Map.Entry<String, IData> getConfig(
            String namespace, String key, Map<String, IData> context) {
        // case 1: no formula defined, return null
        // case 2: derive the data given the formula
        return formulaProvider
                .getFormula(namespace, key)
                .flatMap(f -> f.derive(context))
                .map(d -> new AbstractMap.SimpleEntry<>(key, d))
                .orElse(null);
    }

    /**
     * API to fetch remote configs.
     *
     * @param namespace path to the stored formulas.
     * @param context provided contextual parameters
     * @return remote configs
     */
    public Map<String, IData> getConfigs(String namespace, Map<String, IData> context) {
        HashSet<String> keys = new HashSet<>();
        keys.addAll(context.keySet());
        keys.addAll(formulaProvider.allKeys().getOrDefault(namespace, Collections.emptySet()));
        return FORK_JOIN_POOL
                .submit(
                        () ->
                                keys.parallelStream()
                                        .map(x -> this.getConfig(namespace, x, context))
                                        .filter(Objects::nonNull)
                                        .collect(
                                                Collectors.toConcurrentMap(
                                                        Map.Entry::getKey,
                                                        Map.Entry::getValue,
                                                        (s, a) -> s)))
                .join();
    }
}
