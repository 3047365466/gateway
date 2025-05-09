package com.edan.rapid.common.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Redis配置类，用于初始化Redis客户端连接
 */
public class RedisConfig {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);
    private static RedisClient redisClient;
    private static StatefulRedisConnection<String, String> connection;
    private static StatefulRedisMasterReplicaConnection<String, String> sentinelConnection;
    private static RedisCommands<String, String> syncCommands;
    private static boolean isSentinel = false;

    /**
     * 初始化Redis客户端 - 单节点模式
     *
     * @param host Redis主机地址
     * @param port Redis端口
     * @param password Redis密码，可为null
     * @return 是否初始化成功
     */
    public static boolean initRedisClient(String host, int port, String password, int database) {
        try {
            RedisURI.Builder builder = RedisURI.builder()
                    .withHost(host)
                    .withPort(port)
                    .withDatabase(database)
                    .withTimeout(Duration.ofSeconds(10));

            if (password != null && !password.isEmpty()) {
                builder.withPassword(password);
            }

            RedisURI redisURI = builder.build();
            redisClient = RedisClient.create(redisURI);
            connection = redisClient.connect();
            syncCommands = connection.sync();
            isSentinel = false;

            // log.info("Redis standalone client initialized successfully: {}:{}", host, port);
            return true;
        } catch (Exception e) {
            // log.error("Failed to initialize Redis standalone client: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 初始化Redis客户端 - 哨兵模式
     *
     * @param masterName Redis主节点名称
     * @param sentinels Redis哨兵地址，格式: host1:port1,host2:port2
     * @param password Redis密码，可为null
     * @param database Redis数据库索引
     * @return 是否初始化成功
     */
    public static boolean initRedisSentinel(String masterName, String sentinels, String password,
                                            int database) {
        try {
            // 解析哨兵地址列表
            RedisURI.Builder builder = RedisURI.builder()
                    .withSentinelMasterId(masterName)
                    .withDatabase(database)
                    .withTimeout(Duration.ofSeconds(10));

            // 添加所有哨兵节点
            String[] sentinelList = sentinels.split(",");
            for (String sentinel : sentinelList) {
                String[] hostPort = sentinel.trim().split(":");
                String host = hostPort[0];
                int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 26379; // 默认哨兵端口为26379
                builder.withSentinel(host, port, password);
            }

            if (password != null && !password.isEmpty()) {
                builder.withPassword(password.toCharArray());
            }

            RedisURI redisURI = builder.build();
            redisClient = RedisClient.create();
            sentinelConnection = MasterReplica.connect(redisClient, StringCodec.UTF8, redisURI);
            sentinelConnection.setReadFrom(io.lettuce.core.ReadFrom.MASTER_PREFERRED);
            syncCommands = sentinelConnection.sync();
            isSentinel = true;

            log.info("Redis sentinel client initialized successfully with master: {} and sentinels: {}",
                    masterName, sentinels);
            return true;
        } catch (Exception e) {
            log.error("Failed to initialize Redis sentinel client: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取Redis同步命令接口
     */
    public static RedisCommands<String, String> getRedisCommands() {
        if (syncCommands == null) {
            throw new IllegalStateException("Redis client not initialized. Call initRedisClient or initRedisSentinel first.");
        }
        return syncCommands;
    }

    /**
     * 关闭Redis连接
     */
    public static void shutdown() {
        if (isSentinel && sentinelConnection != null) {
            sentinelConnection.close();
        } else if (connection != null) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        // log.info("Redis connection closed");
    }
}