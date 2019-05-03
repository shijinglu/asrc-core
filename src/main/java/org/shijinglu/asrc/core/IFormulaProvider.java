package org.shijinglu.asrc.core;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Formulas can be stored in a code repository like git or hg, or stored in data base.
 *
 * <p>This interface isolates the Formula providing logic from using logic. And it should take care
 * of cache and caching consistency among different hosts.
 */
public interface IFormulaProvider {

    /**
     * Get all namespace and keys.
     *
     * @return a map whose keys are namespaces and values are keys within the namespace.
     */
    Map<String, Set<String>> allKeys();

    /**
     * Fetch a single formula
     *
     * @param namespace for example namespace can be the codebase name or application name.
     * @param key this is usually the key to a configuration
     * @return formula, empty if none is found
     */
    Optional<IFormula> getFormula(String namespace, String key);
}
