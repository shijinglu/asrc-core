package org.shijinglu.asrc.gateway;

import java.util.Optional;
import org.shijinglu.asrc.core.KVStore;
import redis.clients.jedis.JedisCommands;

public class RedisStore extends KVStore {
    private final IJedisProvider jedisProvider;

    public RedisStore(IJedisProvider jedisProvider) {
        this.jedisProvider = jedisProvider;
    }

    @Override
    public Optional<String> get(String key) {
        JedisCommands jedis = jedisProvider.getJedis();
        Optional<String> res = Optional.ofNullable(jedis.get(key));
        jedisProvider.closeIfNeeded(jedis);
        return res;
    }

    @Override
    public Optional<String> get(String namespace, String key) {
        JedisCommands jedis = jedisProvider.getJedis();
        Optional<String> res = Optional.ofNullable(jedis.hget(namespace, key));
        jedisProvider.closeIfNeeded(jedis);
        return res;
    }
}
