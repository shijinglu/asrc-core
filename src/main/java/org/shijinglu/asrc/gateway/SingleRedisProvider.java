package org.shijinglu.asrc.gateway;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;

/**
 * To use different redis hostname:port, override jvm options: -Dasrc.redis.host=localhost
 * -Dasrc.redis.port=6379
 */
public class SingleRedisProvider implements IJedisProvider {
    private static String REDIS_HOST_PROP_KEY = "asrc.redis.host";
    private static String REDIS_PORT_PROP_KEY = "asrc.redis.port";

    private static String LOCALHOST = "localhost";
    private static Integer DEFAULT_PORT = 6379;
    private final JedisPool pool;

    public SingleRedisProvider() {
        String host = System.getProperty(REDIS_HOST_PROP_KEY, LOCALHOST);
        String portStr = System.getProperty(REDIS_PORT_PROP_KEY, String.valueOf(DEFAULT_PORT));
        pool = new JedisPool(host, Integer.valueOf(portStr));
    }

    @Override
    public Jedis getJedis() {
        return pool.getResource();
    }

    @Override
    public void closeIfNeeded(JedisCommands jedis) {
        if (jedis instanceof Jedis) {
            ((Jedis) jedis).close();
        }
    }
}
