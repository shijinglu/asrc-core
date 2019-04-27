package org.shijinglu.asrc.gateway;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisStoreTest {

    private IJedisProvider prepareProvider() {
        IJedisProvider provider = new SingleRedisProvider();
        JedisCommands jedisClient = provider.getJedis();
        jedisClient.set("hello", "world");
        jedisClient.set("test_key", "test_val");
        jedisClient.hset("test_ns", "test_field_1", "test_hval_1");
        jedisClient.hset("test_ns", "test_field_2", "test_hval_2");
        provider.closeIfNeeded(jedisClient);
        return provider;
    }

    @Test
    public void testGetString() {
        try {
            RedisStore store = new RedisStore(prepareProvider());
            Assert.assertEquals(store.get("hello").get(), "world");
            Assert.assertEquals(store.get("test_key").get(), "test_val");
            Assert.assertEquals(store.get("test_ns", "test_field_1").get(), "test_hval_1");
            Assert.assertEquals(store.get("test_ns", "test_field_2").get(), "test_hval_2");
        } catch (JedisConnectionException e) {
            Logger.getLogger(RedisStoreTest.class.getName())
                    .log(
                            Level.SEVERE,
                            "Test effectively failed, please make sure jedis server is up and runing");
        }
    }
}
