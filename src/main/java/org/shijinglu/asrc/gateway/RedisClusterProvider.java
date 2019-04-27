package org.shijinglu.asrc.gateway;

import java.util.HashSet;
import java.util.Set;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClusterProvider implements IJedisProvider {
    private static String REDIS_CLUSTER_HNP_DEFAULT =
            "localhost:6379,localhost:6380,localhost:6381";
    private static String REDIS_CLUSTER_HNP_PROP_KEY = "asrc.redis_cluster.hosts_and_ports";

    private static final int DEFAULT_TIMEOUT = 2000;
    private static final JedisPoolConfig DEFAULT_CONFIG = new JedisPoolConfig();

    private final JedisCluster jedisCluster;

    public RedisClusterProvider() {
        jedisCluster = getCluster();
    }

    private static JedisCluster getCluster() {
        String hnpProp = System.getProperty(REDIS_CLUSTER_HNP_PROP_KEY, REDIS_CLUSTER_HNP_DEFAULT);
        Set<HostAndPort> nodes = new HashSet<>();

        for (String hnpRaw : hnpProp.split(",")) {
            String[] hnpPair = hnpRaw.split(":");
            String host = hnpPair[0].trim();
            int port = Integer.valueOf(hnpPair[1].trim());
            nodes.add(new HostAndPort(host, port));
        }
        return new JedisCluster(nodes, DEFAULT_TIMEOUT, DEFAULT_CONFIG);
    }

    @Override
    public JedisCommands getJedis() {
        return jedisCluster;
    }

    @Override
    public void closeIfNeeded(JedisCommands jedis) {}
}
