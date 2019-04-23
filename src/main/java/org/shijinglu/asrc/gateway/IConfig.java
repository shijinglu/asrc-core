package org.shijinglu.asrc.gateway;

import java.util.Optional;
import java.util.Set;

/**
 * ASYC overrides existing configuration with server provided values.
 * This is the protocol that ASYC uses to fetch and to provide configurations.
 */
public interface IConfig {

    Optional<Boolean> getBool(String key);

    Optional<Integer> getInteger(String key);

    Optional<Double> getDouble(String key);

    Optional<String> getString(String key);

    Set<String> allKeys();
}
