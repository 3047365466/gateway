package com.edan.rapid.common.redis;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.logging.log4j.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Redis限流器实现
 */

public class RateLimiter {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * 令牌桶算法限流脚本（Lua）
     */
    private static final String TOKEN_BUCKET_SCRIPT = 
            "local key = KEYS[1] " +
            "local max_tokens = tonumber(ARGV[1]) " +
            "local tokens_per_second = tonumber(ARGV[2]) " +
            "local now = tonumber(redis.call('time')[1]) " +
            "local tokens_key = key .. ':tokens' " +
            "local timestamp_key = key .. ':timestamp' " +
            
            "local last_tokens = tonumber(redis.call('get', tokens_key) or max_tokens) " +
            "local last_refreshed = tonumber(redis.call('get', timestamp_key) or 0) " +
            
            "local delta = math.max(0, now - last_refreshed) " +
            "local filled_tokens = math.min(max_tokens, last_tokens + (delta * tokens_per_second)) " +
            
            "local allowed = filled_tokens >= 1 " +
            "local new_tokens = filled_tokens " +
            
            "if allowed then " +
            "    new_tokens = filled_tokens - 1 " +
            "end " +
            
            "redis.call('setex', tokens_key, 3600, new_tokens) " +
            "redis.call('setex', timestamp_key, 3600, now) " +
            
            "return allowed and 1 or 0";

    /**
     * 固定窗口算法限流脚本（Lua）
     */
    private static final String FIXED_WINDOW_SCRIPT = 
            "local key = KEYS[1] " +
            "local max_requests = tonumber(ARGV[1]) " +
            "local window_size = tonumber(ARGV[2]) " +
            
            "local current = tonumber(redis.call('get', key) or 0) " +
            
            "if current < max_requests then " +
            "    redis.call('incr', key) " +
            "    if current == 0 then " +
            "        redis.call('expire', key, window_size) " +
            "    end " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 滑动窗口算法限流脚本（Lua）
     */
    private static final String SLIDING_WINDOW_SCRIPT = 
            "local key = KEYS[1] " +
            "local max_requests = tonumber(ARGV[1]) " +
            "local window_size_ms = tonumber(ARGV[2]) " +
            "local now = redis.call('time') " +
            "local timestamp = tonumber(now[1]) * 1000 + tonumber(now[2])/1000 " +
            "local window_start = timestamp - window_size_ms " +
            
            "redis.call('zremrangebyscore', key, 0, window_start) " +
            
            "local count = redis.call('zcard', key) " +
            
            "if count < max_requests then " +
            "    redis.call('zadd', key, timestamp, timestamp..':'..math.random()) " +
            "    redis.call('expire', key, math.ceil(window_size_ms/1000)) " +
            "    return 1 " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 漏桶算法限流脚本（Lua）
     */
    private static final String LEAKY_BUCKET_SCRIPT = 
            "local key = KEYS[1] " +
            "local capacity = tonumber(ARGV[1]) " +
            "local rate = tonumber(ARGV[2]) " +
            "local now = tonumber(redis.call('time')[1]) " +
            "local water_key = key .. ':water' " +
            "local timestamp_key = key .. ':timestamp' " +
            
            "local last_water = tonumber(redis.call('get', water_key) or 0) " +
            "local last_time = tonumber(redis.call('get', timestamp_key) or now) " +
            
            "local delta_time = now - last_time " +
            "local out_water = math.min(last_water, delta_time * rate) " +
            "local current_water = last_water - out_water + 1 " +
            
            "local allowed = current_water <= capacity " +
            
            "if allowed then " +
            "    redis.call('setex', water_key, 3600, current_water) " +
            "    redis.call('setex', timestamp_key, 3600, now) " +
            "    return 1 " +
            "else " +
            "    redis.call('setex', water_key, 3600, last_water) " +
            "    redis.call('setex', timestamp_key, 3600, now) " +
            "    return 0 " +
            "end";

    /**
     * 计数器限流脚本（Lua）- 简单实现
     */
    private static final String COUNTER_SCRIPT = 
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local expire_time = tonumber(ARGV[2]) " +
            
            "local count = redis.call('incr', key) " +
            
            "if count == 1 then " +
            "    redis.call('expire', key, expire_time) " +
            "end " +
            
            "if count <= limit then " +
            "    return 1 " +
            "else " +
            "    if count == limit + 1 then " +
            "        -- 恢复计数，避免溢出 " +
            "        redis.call('decr', key) " +
            "    end " +
            "    return 0 " +
            "end";

    /**
     * 分布式并发限流脚本（Lua）
     */
    private static final String CONCURRENT_SCRIPT = 
            "local key = KEYS[1] " +
            "local max_concurrent = tonumber(ARGV[1]) " +
            "local id = ARGV[2] " +
            "local timeout = tonumber(ARGV[3]) " +
            
            "local count = redis.call('zcard', key) " +
            
            "if count < max_concurrent then " +
            "    redis.call('zadd', key, timeout, id) " +
            "    return 1 " +
            "else " +
            "    -- 移除过期的ID " +
            "    local now = tonumber(redis.call('time')[1]) " +
            "    redis.call('zremrangebyscore', key, 0, now) " +
            "    -- 重新检查是否有空间 " +
            "    count = redis.call('zcard', key) " +
            "    if count < max_concurrent then " +
            "        redis.call('zadd', key, timeout, id) " +
            "        return 1 " +
            "    end " +
            "    return 0 " +
            "end";

    /**
     * 带权重的令牌桶限流脚本（Lua）
     */
    private static final String WEIGHTED_TOKEN_BUCKET_SCRIPT = 
            "local key = KEYS[1] " +
            "local max_tokens = tonumber(ARGV[1]) " +
            "local tokens_per_second = tonumber(ARGV[2]) " +
            "local weight = tonumber(ARGV[3]) " +
            "local now = tonumber(redis.call('time')[1]) " +
            "local tokens_key = key .. ':tokens' " +
            "local timestamp_key = key .. ':timestamp' " +
            
            "local last_tokens = tonumber(redis.call('get', tokens_key) or max_tokens) " +
            "local last_refreshed = tonumber(redis.call('get', timestamp_key) or 0) " +
            
            "local delta = math.max(0, now - last_refreshed) " +
            "local filled_tokens = math.min(max_tokens, last_tokens + (delta * tokens_per_second)) " +
            
            "local allowed = filled_tokens >= weight " +
            "local new_tokens = filled_tokens " +
            
            "if allowed then " +
            "    new_tokens = filled_tokens - weight " +
            "end " +
            
            "redis.call('setex', tokens_key, 3600, new_tokens) " +
            "redis.call('setex', timestamp_key, a3600, now) " +
            
            "return allowed and 1 or 0";

    /**
     * 使用令牌桶算法判断是否允许访问
     * 
     * @param key 限流的键
     * @param maxTokens 令牌桶容量
     * @param tokensPerSecond 每秒生成的令牌数
     * @return 是否允许访问
     */
    public static boolean tryAcquire(String key, int maxTokens, int tokensPerSecond) {
        try {
            RedisCommands<String, String> commands = RedisConfig.getRedisCommands();
            List<String> keys = Arrays.asList(key);
            List<String> args = Arrays.asList(
                    String.valueOf(maxTokens),
                    String.valueOf(tokensPerSecond));
            
            Long result = commands.eval(
                    TOKEN_BUCKET_SCRIPT, 
                    ScriptOutputType.INTEGER,
                    keys.toArray(new String[0]),
                    args.toArray(new String[0]));
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error in Redis rate limiting (token bucket): " + e.getMessage(), e);
            // 限流失败时，为了保护系统，默认限流
            return false;
        }
    }

    /**
     * 使用固定窗口算法判断是否允许访问
     * 
     * @param key 限流的键
     * @param maxRequests 窗口期内最大请求数
     * @param windowSize 窗口大小（秒）
     * @return 是否允许访问
     */
    public static boolean tryAcquireWithFixedWindow(String key, int maxRequests, int windowSize) {
        try {
            RedisCommands<String, String> commands = RedisConfig.getRedisCommands();
            List<String> keys = Arrays.asList(key);
            List<String> args = Arrays.asList(
                    String.valueOf(maxRequests),
                    String.valueOf(windowSize));
            
            Long result = commands.eval(
                    FIXED_WINDOW_SCRIPT, 
                    ScriptOutputType.INTEGER,
                    keys.toArray(new String[0]),
                    args.toArray(new String[0]));
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error in Redis rate limiting (fixed window): " + e.getMessage(), e);
            // 限流失败时，为了保护系统，默认限流
            return false;
        }
    }
    
    /**
     * 使用滑动窗口算法判断是否允许访问
     * 
     * @param key 限流的键
     * @param maxRequests 窗口期内最大请求数
     * @param windowSizeInMillis 窗口大小（毫秒）
     * @return 是否允许访问
     */
    public static boolean tryAcquireWithSlidingWindow(String key, int maxRequests, long windowSizeInMillis) {
        try {
            RedisCommands<String, String> commands = RedisConfig.getRedisCommands();
            List<String> keys = Arrays.asList(key);
            List<String> args = Arrays.asList(
                    String.valueOf(maxRequests),
                    String.valueOf(windowSizeInMillis));
            
            Long result = commands.eval(
                    SLIDING_WINDOW_SCRIPT, 
                    ScriptOutputType.INTEGER,
                    keys.toArray(new String[0]),
                    args.toArray(new String[0]));
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error in Redis rate limiting (sliding window): " + e.getMessage(), e);
            // 限流失败时，为了保护系统，默认限流
            return false;
        }
    }

    /**
     * 使用漏桶算法判断是否允许访问
     * 
     * @param key 限流的键
     * @param capacity 漏桶容量
     * @param rate 漏水速率（每秒）
     * @return 是否允许访问
     */
    public static boolean tryAcquireWithLeakyBucket(String key, int capacity, double rate) {
        try {
            RedisCommands<String, String> commands = RedisConfig.getRedisCommands();
            List<String> keys = Arrays.asList(key);
            List<String> args = Arrays.asList(
                    String.valueOf(capacity),
                    String.valueOf(rate));
            
            Long result = commands.eval(
                    LEAKY_BUCKET_SCRIPT, 
                    ScriptOutputType.INTEGER,
                    keys.toArray(new String[0]),
                    args.toArray(new String[0]));
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error in Redis rate limiting (leaky bucket): " + e.getMessage(), e);
            // 限流失败时，为了保护系统，默认限流
            return false;
        }
    }

    /**
     * 使用计数器算法判断是否允许访问
     * 
     * @param key 限流的键
     * @param limit 限制次数
     * @param expireTime 过期时间（秒）
     * @return 是否允许访问
     */
    public static boolean tryAcquireWithCounter(String key, int limit, int expireTime) {
        try {
            RedisCommands<String, String> commands = RedisConfig.getRedisCommands();
            List<String> keys = Arrays.asList(key);
            List<String> args = Arrays.asList(
                    String.valueOf(limit),
                    String.valueOf(expireTime));
            
            Long result = commands.eval(
                    COUNTER_SCRIPT, 
                    ScriptOutputType.INTEGER,
                    keys.toArray(new String[0]),
                    args.toArray(new String[0]));
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error in Redis rate limiting (counter): " + e.getMessage(), e);
            // 限流失败时，为了保护系统，默认限流
            return false;
        }
    }

    /**
     * 使用分布式并发限流算法判断是否允许访问
     * 
     * @param key 限流的键
     * @param maxConcurrent 最大并发数
     * @param id 请求唯一标识
     * @param timeout 过期时间（秒）
     * @return 是否允许访问
     */
    public static boolean tryAcquireWithConcurrent(String key, int maxConcurrent, String id, int timeout) {
        try {
            RedisCommands<String, String> commands = RedisConfig.getRedisCommands();
            List<String> keys = Arrays.asList(key);
            // 当前时间加上timeout作为过期时间
            int expireAt = (int)(System.currentTimeMillis() / 1000) + timeout;
            List<String> args = Arrays.asList(
                    String.valueOf(maxConcurrent),
                    id,
                    String.valueOf(expireAt));
            
            Long result = commands.eval(
                    CONCURRENT_SCRIPT, 
                    ScriptOutputType.INTEGER,
                    keys.toArray(new String[0]),
                    args.toArray(new String[0]));
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error in Redis rate limiting (concurrent): " + e.getMessage(), e);
            // 限流失败时，为了保护系统，默认限流
            return false;
        }
    }

    /**
     * 使用带权重的令牌桶算法判断是否允许访问
     * 
     * @param key 限流的键
     * @param maxTokens 令牌桶容量
     * @param tokensPerSecond 每秒生成的令牌数
     * @param weight 请求权重
     * @return 是否允许访问
     */
    public static boolean tryAcquireWithWeight(String key, int maxTokens, int tokensPerSecond, int weight) {
        try {
            RedisCommands<String, String> commands = RedisConfig.getRedisCommands();
            List<String> keys = Arrays.asList(key);
            List<String> args = Arrays.asList(
                    String.valueOf(maxTokens),
                    String.valueOf(tokensPerSecond),
                    String.valueOf(weight));
            
            Long result = commands.eval(
                    WEIGHTED_TOKEN_BUCKET_SCRIPT, 
                    ScriptOutputType.INTEGER,
                    keys.toArray(new String[0]),
                    args.toArray(new String[0]));
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error in Redis rate limiting (weighted token bucket): " + e.getMessage(), e);
            // 限流失败时，为了保护系统，默认限流
            return false;
        }
    }

    /**
     * 使用令牌桶算法判断是否允许访问
     *
     * @param algorithm 算法选择
     * @param keys 键列
     * @param args 值列
     * @param lua lua脚本
     * @return 是否允许访问
     */
    public static boolean tryAcquireCustomize(String algorithm, List<String> keys, List<String> args, String lua) {
        try {
            RedisCommands<String, String> commands = RedisConfig.getRedisCommands();
            if (!algorithm.isEmpty()) {
                String luaMatch = luaMatch(algorithm);
                lua = luaMatch == null ? lua : luaMatch;
            }
            // lua 脚本不为空
            Assert.isNonEmpty(lua);
            Long result = commands.eval(
                    lua,
                    ScriptOutputType.INTEGER,
                    keys.toArray(new String[0]),
                    args.toArray(new String[0]));

            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error in Redis rate limiting (token bucket): " + e.getMessage(), e);
            // 限流失败时，为了保护系统，默认限流
            return false;
        }
    }

    private static String luaMatch(String algorithm) {
        switch (algorithm) {
            case "TOKEN_BUCKET_SCRIPT":
                return TOKEN_BUCKET_SCRIPT;
            case "FIXED_WINDOW_SCRIPT":
                return FIXED_WINDOW_SCRIPT;
            case "SLIDING_WINDOW_SCRIPT":
                return SLIDING_WINDOW_SCRIPT;
            case "LEAKY_BUCKET_SCRIPT":
                return LEAKY_BUCKET_SCRIPT;
            case "COUNTER_SCRIPT":
                return COUNTER_SCRIPT;
            case "CONCURRENT_SCRIPT":
                return CONCURRENT_SCRIPT;
            case "WEIGHTED_TOKEN_BUCKET_SCRIPT":
                return WEIGHTED_TOKEN_BUCKET_SCRIPT;
            default:
                return null;
        }
    }
} 