package org.shijinglu.asrc.gateway;

import redis.clients.jedis.JedisCommands;

public interface IJedisProvider {

    /** Provide a client that can retrieve data from sharded or non-sharded Jedis */
    JedisCommands getJedis();

    void closeIfNeeded(JedisCommands jedis);
}
