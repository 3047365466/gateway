package com.edan.rapid.core.limiting;

import com.edan.rapid.common.redis.RedisConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 限流器工厂，用于创建不同类型的限流器
 */
@Slf4j
public class RateLimiterFactory {

    /**
     * 限流器类型枚举
     */
    public enum LimiterType {
        /**
         * 本地内存限流器
         */
        LOCAL,
        
        /**
         * Redis限流器
         */
        REDIS,
        
        /**
         * Redis哨兵模式限流器
         */
        REDIS_SENTINEL
    }
    
    /**
     * 创建限流器
     * 
     * @param type 限流器类型
     * @param config 配置参数
     * @return 限流器实例
     */
    public static RateLimiterService createLimiter(LimiterType type, RateLimiterConfig config) {
        switch (type) {
            case REDIS:
            case REDIS_SENTINEL:
                RedisRateLimiterService service = new RedisRateLimiterService(config, type);
                if (!checkRedisConnection()) {
                    log.warn("Redis connection not available, falling back to local rate limiter");
                    return new LocalRateLimiterService(config);
                }
                return service;
            case LOCAL:
            default:
                return new LocalRateLimiterService(config);
        }
    }
    
    /**
     * 检查Redis连接是否可用
     */
    private static boolean checkRedisConnection() {
        try {
            // 尝试执行PING命令测试连接
            String result = RedisConfig.getRedisCommands().ping();
            return "PONG".equalsIgnoreCase(result);
        } catch (Exception e) {
            log.error("Redis connection check failed: " + e.getMessage(), e);
            return false;
        }
    }
} 