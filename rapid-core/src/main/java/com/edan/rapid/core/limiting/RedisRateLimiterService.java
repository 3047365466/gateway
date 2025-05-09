package com.edan.rapid.core.limiting;

import com.edan.rapid.common.redis.RateLimiter;
import com.edan.rapid.common.redis.RedisConfig;
import com.edan.rapid.core.RapidConfig;
import com.edan.rapid.core.RapidConfigLoader;
import com.edan.rapid.core.netty.processor.filter.pre.RateLimitPreFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 基于Redis的限流器服务实现
 */
@Slf4j
public class RedisRateLimiterService implements RateLimiterService {

    private RateLimiterConfig config;

    private RateLimiterFactory.LimiterType type = RateLimiterFactory.LimiterType.REDIS_SENTINEL;
    
    public RedisRateLimiterService(RateLimiterConfig config, RateLimiterFactory.LimiterType type) {
        this.config = config;
        this.type = type;
        init();
    }
    // 只适用于全局过滤器初始化失败时使用作为局部过滤器使用
    public RedisRateLimiterService() {
        init();
    }

    
    @Override
    public boolean tryAcquire(String limitKey) {

        switch (config.getAlgorithm()) {
            case TOKEN_BUCKET:
                return tryAcquireWithTokenBucket(limitKey, config.getMaxTokens(), config.getTokensPerSecond());
            case FIXED_WINDOW:
                return tryAcquireWithFixedWindow(limitKey, config.getMaxRequests(), config.getWindowSize());
            case SLIDING_WINDOW:
                return tryAcquireWithSlidingWindow(limitKey, config.getMaxRequests(), config.getWindowSize() * 1000);
            case LEAKY_BUCKET:
                return tryAcquireWithLeakyBucket(limitKey, config.getMaxTokens(), config.getTokensPerSecond());
            case COUNTER:
                return tryAcquireWithCounter(limitKey, config.getMaxRequests(), config.getWindowSize());
            case CONCURRENT:
                // 为并发限流生成唯一请求ID
                String requestId = UUID.randomUUID().toString();
                return tryAcquireWithConcurrent(limitKey, config.getMaxRequests(), requestId, config.getWindowSize());
            case WEIGHTED_TOKEN_BUCKET:
                // 对于带权重的令牌桶，默认权重为1
                return tryAcquireWithWeight(limitKey, config.getMaxTokens(), config.getTokensPerSecond(), 1);
            default:
                return tryAcquireWithTokenBucket(limitKey, config.getMaxTokens(), config.getTokensPerSecond());
        }
    }

    @Override
    public boolean tryAcquireByPreFilter(Object parameter) {
        RateLimitPreFilter.Config.LuaParameter luaParameter = (RateLimitPreFilter.Config.LuaParameter) parameter;

        return RateLimiter.tryAcquireCustomize(luaParameter.getAlgorithm(), luaParameter.getKeys(),
                luaParameter.getValues(), luaParameter.getLua());
    }
    
    @Override
    public boolean tryAcquireWithTokenBucket(String key, int maxTokens, int tokensPerSecond) {
        return RateLimiter.tryAcquire(key, maxTokens, tokensPerSecond);
    }
    
    @Override
    public boolean tryAcquireWithFixedWindow(String key, int maxRequests, int windowSize) {
        return RateLimiter.tryAcquireWithFixedWindow(key, maxRequests, windowSize);
    }
    
    @Override
    public boolean tryAcquireWithSlidingWindow(String key, int maxRequests, int windowSizeInMillis) {
        return RateLimiter.tryAcquireWithSlidingWindow(key, maxRequests, windowSizeInMillis);
    }
    
    @Override
    public boolean tryAcquireWithLua(String key, int maxRequests, int windowSizeInMillis) {
        // 此方法保持为后续自定义实现预留
        return false;
    }
    
    @Override
    public boolean tryAcquireWithLeakyBucket(String key, int capacity, double rate) {
        return RateLimiter.tryAcquireWithLeakyBucket(key, capacity, rate);
    }
    
    @Override
    public boolean tryAcquireWithCounter(String key, int limit, int expireTime) {
        return RateLimiter.tryAcquireWithCounter(key, limit, expireTime);
    }
    
    @Override
    public boolean tryAcquireWithConcurrent(String key, int maxConcurrent, String id, int timeout) {
        return RateLimiter.tryAcquireWithConcurrent(key, maxConcurrent, id, timeout);
    }
    
    @Override
    public boolean tryAcquireWithWeight(String key, int maxTokens, int tokensPerSecond, int weight) {
        return RateLimiter.tryAcquireWithWeight(key, maxTokens, tokensPerSecond, weight);
    }

    @Override
    public void init() {
        RapidConfig rapidConfig = RapidConfigLoader.getRapidConfig();
        switch (this.type) {
            case REDIS:
                RedisConfig.initRedisClient(rapidConfig.getRedisHost(), rapidConfig.getPort(),
                        rapidConfig.getRedisPassword(), rapidConfig.getRedisDatabase());
                break;
            case REDIS_SENTINEL:
                // 优先使用redisSentinels（如果已设置）
                String sentinels = rapidConfig.getRedisSentinels();
                if (sentinels == null || sentinels.isEmpty()) {
                    // 如果没有设置redisSentinels，则使用单个redisHost和redisPort
                    sentinels = rapidConfig.getRedisHost();
                    if (rapidConfig.getRedisPort() != 26379) {
                        // 如果端口不是默认的26379，确保添加到地址中
                        sentinels = sentinels + ":" + rapidConfig.getRedisPort();
                    }
                }
                log.info("Initializing Redis Sentinel with sentinels: {}", sentinels);
                RedisConfig.initRedisSentinel(rapidConfig.getRedisMasterName(), sentinels,
                        rapidConfig.getRedisPassword(),
                        rapidConfig.getRedisDatabase());
                break;
        }
    }
    
    @Override
    public void shutdown() {
        log.info("Redis rate limiter shutdown");
        RedisConfig.shutdown();
    }
} 