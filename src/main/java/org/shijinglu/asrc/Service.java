package org.shijinglu.asrc;

import com.google.common.annotations.VisibleForTesting;
import org.shijinglu.asrc.gateway.IFormulaProvider;
import org.shijinglu.asrc.gateway.IContextProvider;
import org.shijinglu.asrc.model.Formula;
import org.shijinglu.asrc.model.IData;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Service {
    private final IFormulaProvider formulaProvider;
    private final IContextProvider contextProvider;

    public Service(IFormulaProvider formulaProvider, IContextProvider contextProvider) {
        this.formulaProvider = formulaProvider;
        this.contextProvider = contextProvider;

    }

    /**
     * Calculate the remote config data given key and context.
     * @return null if no matching formula is found or no corresponding formula at all
     */
    @VisibleForTesting
    protected Map.Entry<String, IData> getConfig(String namespace, String key, Map<String, IData> context) {
        // case 1: no formula defined, return null
        Formula f = formulaProvider.getFormula(namespace, key);
        if (f == null) {
            return null;
        }
        // case 2: derive the data given the formula
        IData res = f.derive(context);
        if (res == null) {
            return null;
        }
        return new AbstractMap.SimpleEntry<>(key, res);
    }

    /**
     * API to fetch remote configs.
     * @param namespace path to the stored formulas.
     * @param context provided contextual parameters
     * @return remote configs
     * @throws IllegalArgumentException
     */
    public Map<String, IData> getConfigs(String namespace, Map<String, IData> context) throws IllegalArgumentException {
        return context.entrySet()
                .parallelStream()
                .map(x -> this.getConfig(namespace, x.getKey(), context))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
