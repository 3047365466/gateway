package com.edan.rapid.core.limiting;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于本地内存的限流器服务实现
 */
@Slf4j
public class LocalRateLimiterService implements RateLimiterService {

    private final RateLimiterConfig config;
    
    // 令牌桶缓存
    private final ConcurrentHashMap<String, RateLimiter> tokenBucketLimiters = new ConcurrentHashMap<>();
    
    // 固定窗口计数器缓存
    private LoadingCache<String, AtomicInteger> fixedWindowCounters;
    
    // 滑动窗口缓存 - 使用Guava Cache实现简单滑动窗口
    private Cache<String, AtomicInteger> slidingWindowCounters;
    
    // 漏桶缓存
    private final ConcurrentHashMap<String, LeakyBucket> leakyBuckets = new ConcurrentHashMap<>();
    
    // 计数器缓存
    private LoadingCache<String, AtomicInteger> counters;
    
    // 并发限流缓存
    private final ConcurrentHashMap<String, ConcurrentLimiter> concurrentLimiters = new ConcurrentHashMap<>();
    
    // 带权重的令牌桶缓存
    private final ConcurrentHashMap<String, WeightedTokenBucket> weightedTokenBuckets = new ConcurrentHashMap<>();
    
    public LocalRateLimiterService(RateLimiterConfig config) {
        this.config = config;
        init();
    }
    
    @Override
    public void init() {
        // 初始化固定窗口缓存，设置过期时间
        fixedWindowCounters = CacheBuilder.newBuilder()
                .expireAfterWrite(config.getWindowSize(), TimeUnit.SECONDS)
                .build(new CacheLoader<String, AtomicInteger>() {
                    @Override
                    public AtomicInteger load(String key) {
                        return new AtomicInteger(0);
                    }
                });
        
        // 初始化滑动窗口缓存，过期时间设置为窗口大小的一小部分，实现滑动效果
        slidingWindowCounters = CacheBuilder.newBuilder()
                .expireAfterWrite(config.getWindowSize() / 10, TimeUnit.SECONDS)
                .build();
        
        // 初始化计数器缓存
        counters = CacheBuilder.newBuilder()
                .expireAfterWrite(config.getWindowSize(), TimeUnit.SECONDS)
                .build(new CacheLoader<String, AtomicInteger>() {
                    @Override
                    public AtomicInteger load(String key) {
                        return new AtomicInteger(0);
                    }
                });
        
        log.info("Local rate limiter initialized with algorithm: {}", config.getAlgorithm());
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
                // 为并发限流生成请求ID，使用当前线程ID作为标识
                String requestId = String.valueOf(Thread.currentThread().getId());
                return tryAcquireWithConcurrent(limitKey, config.getMaxRequests(), requestId, config.getWindowSize());
            case WEIGHTED_TOKEN_BUCKET:
                // 默认权重为1
                return tryAcquireWithWeight(limitKey, config.getMaxTokens(), config.getTokensPerSecond(), 1);
            default:
                return tryAcquireWithTokenBucket(limitKey, config.getMaxTokens(), config.getTokensPerSecond());
        }
    }

    @Override
    public boolean tryAcquireByPreFilter(Object parameter) {
        return true;
    }

    @Override
    public boolean tryAcquireWithTokenBucket(String key, int maxTokens, int tokensPerSecond) {
        // 从缓存获取或创建令牌桶
        RateLimiter limiter = tokenBucketLimiters.computeIfAbsent(key, k -> 
                RateLimiter.create(tokensPerSecond, 1, TimeUnit.SECONDS));
        
        // 尝试获取令牌，不等待
        return limiter.tryAcquire();
    }
    
    @Override
    public boolean tryAcquireWithFixedWindow(String key, int maxRequests, int windowSize) {
        try {
            AtomicInteger counter = fixedWindowCounters.get(key);
            int current = counter.incrementAndGet();
            
            // 如果超过最大请求数，则减回去并返回失败
            if (current > maxRequests) {
                counter.decrementAndGet();
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error in fixed window limiter: " + e.getMessage(), e);
            return true; // 出错时默认放行
        }
    }
    
    @Override
    public boolean tryAcquireWithSlidingWindow(String key, int maxRequests, int windowSizeInMillis) {
        try {
            // 计算当前时间区块
            long now = System.currentTimeMillis();
            String blockKey = key + ":" + (now / (windowSizeInMillis / 10));
            
            // 增加当前区块的计数
            AtomicInteger counter = new AtomicInteger(1);
            slidingWindowCounters.put(blockKey, counter);
            
            // 计算所有活跃区块的请求总数
            int totalRequests = 0;
            for (AtomicInteger value : slidingWindowCounters.asMap().values()) {
                totalRequests += value.get();
            }
            
            // 如果超过最大请求数，则减回去并返回失败
            if (totalRequests > maxRequests) {
                counter.decrementAndGet();
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error in sliding window limiter: " + e.getMessage(), e);
            return true; // 出错时默认放行
        }
    }

    @Override
    public boolean tryAcquireWithLua(String key, int maxRequests, int windowSizeInMillis) {
        // 本地实现不支持Lua脚本
        return true;
    }
    
    @Override
    public boolean tryAcquireWithLeakyBucket(String key, int capacity, double rate) {
        try {
            LeakyBucket bucket = leakyBuckets.computeIfAbsent(key, k -> new LeakyBucket(capacity, rate));
            return bucket.tryAcquire();
        } catch (Exception e) {
            log.error("Error in leaky bucket limiter: " + e.getMessage(), e);
            return false; // 出错时默认限流
        }
    }
    
    @Override
    public boolean tryAcquireWithCounter(String key, int limit, int expireTime) {
        try {
            AtomicInteger counter = counters.get(key);
            int current = counter.incrementAndGet();
            
            // 如果超过限制，则减回去并返回失败
            if (current > limit) {
                counter.decrementAndGet();
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error in counter limiter: " + e.getMessage(), e);
            return false; // 出错时默认限流
        }
    }
    
    @Override
    public boolean tryAcquireWithConcurrent(String key, int maxConcurrent, String id, int timeout) {
        try {
            ConcurrentLimiter limiter = concurrentLimiters.computeIfAbsent(key, k -> new ConcurrentLimiter(maxConcurrent));
            return limiter.acquire(id, timeout);
        } catch (Exception e) {
            log.error("Error in concurrent limiter: " + e.getMessage(), e);
            return false; // 出错时默认限流
        }
    }
    
    @Override
    public boolean tryAcquireWithWeight(String key, int maxTokens, int tokensPerSecond, int weight) {
        try {
            WeightedTokenBucket bucket = weightedTokenBuckets.computeIfAbsent(key, 
                    k -> new WeightedTokenBucket(maxTokens, tokensPerSecond));
            return bucket.tryAcquire(weight);
        } catch (Exception e) {
            log.error("Error in weighted token bucket limiter: " + e.getMessage(), e);
            return false; // 出错时默认限流
        }
    }

    @Override
    public void shutdown() {
        tokenBucketLimiters.clear();
        fixedWindowCounters.invalidateAll();
        slidingWindowCounters.invalidateAll();
        leakyBuckets.clear();
        counters.invalidateAll();
        concurrentLimiters.clear();
        weightedTokenBuckets.clear();
        log.info("Local rate limiter shutdown");
    }
    
    /**
     * 漏桶实现
     */
    private static class LeakyBucket {
        private final int capacity;
        private final double rate;
        private double water;
        private long lastLeakTimestamp;
        
        public LeakyBucket(int capacity, double rate) {
            this.capacity = capacity;
            this.rate = rate;
            this.water = 0;
            this.lastLeakTimestamp = System.currentTimeMillis();
        }
        
        public synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            double timeElapsed = (now - lastLeakTimestamp) / 1000.0;
            
            // 计算漏出的水量
            double leaked = timeElapsed * rate;
            water = Math.max(0, water - leaked);
            lastLeakTimestamp = now;
            
            // 如果还有容量，则放入一滴水并返回成功
            if (water < capacity) {
                water += 1;
                return true;
            }
            
            return false;
        }
    }
    
    /**
     * 并发限流实现
     */
    private static class ConcurrentLimiter {
        private final int maxConcurrent;
        private final Map<String, Long> activeRequests = new ConcurrentHashMap<>();
        
        public ConcurrentLimiter(int maxConcurrent) {
            this.maxConcurrent = maxConcurrent;
        }
        
        public synchronized boolean acquire(String id, int timeout) {
            // 清理过期的请求
            long now = System.currentTimeMillis() / 1000;
            activeRequests.entrySet().removeIf(entry -> entry.getValue() < now);
            
            // 检查是否已达到最大并发数
            if (activeRequests.size() >= maxConcurrent) {
                return false;
            }
            
            // 记录请求，并设置过期时间
            activeRequests.put(id, now + timeout);
            return true;
        }
    }
    
    /**
     * 带权重的令牌桶实现
     */
    private static class WeightedTokenBucket {
        private final int maxTokens;
        private final double tokensPerSecond;
        private double tokens;
        private long lastRefillTimestamp;
        
        public WeightedTokenBucket(int maxTokens, double tokensPerSecond) {
            this.maxTokens = maxTokens;
            this.tokensPerSecond = tokensPerSecond;
            this.tokens = maxTokens;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }
        
        public synchronized boolean tryAcquire(int weight) {
            // 先补充令牌
            refill();
            
            // 如果令牌数不足，则返回失败
            if (tokens < weight) {
                return false;
            }
            
            // 消耗指定权重的令牌
            tokens -= weight;
            return true;
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            double timeElapsed = (now - lastRefillTimestamp) / 1000.0;
            
            // 计算需要补充的令牌数
            double tokensToAdd = timeElapsed * tokensPerSecond;
            
            // 更新令牌数，不超过最大容量
            tokens = Math.min(maxTokens, tokens + tokensToAdd);
            lastRefillTimestamp = now;
        }
    }
} 