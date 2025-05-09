package com.edan.rapid.core.limiting;

/**
 * 限流器服务接口
 */
public interface RateLimiterService {

    /**
     * 全局过滤器使用 尝试获取令牌（默认令牌桶算法）
     *
     * @param key 限流键
     * @return 是否获取成功
     */
    boolean tryAcquire(String key);

    /**
     * 前置过滤器使用 尝试获取令牌（默认令牌桶算法）
     *
     * @return 是否获取成功
     */
    boolean tryAcquireByPreFilter(Object parameter);

    /**
     * 使用令牌桶算法尝试获取令牌
     *
     * @param key 限流键
     * @param maxTokens 令牌桶容量
     * @param tokensPerSecond 每秒生成的令牌数
     * @return 是否获取成功
     */
    boolean tryAcquireWithTokenBucket(String key, int maxTokens, int tokensPerSecond);

    /**
     * 使用固定窗口算法尝试获取令牌
     *
     * @param key 限流键
     * @param maxRequests 窗口内最大请求数
     * @param windowSize 窗口大小（秒）
     * @return 是否获取成功
     */
    boolean tryAcquireWithFixedWindow(String key, int maxRequests, int windowSize);

    /**
     * 使用滑动窗口算法尝试获取令牌
     *
     * @param key 限流键
     * @param maxRequests 窗口内最大请求数
     * @param windowSizeInMillis 窗口大小（毫秒）
     * @return 是否获取成功
     */
    boolean tryAcquireWithSlidingWindow(String key, int maxRequests, int windowSizeInMillis);

    /**
     * 使用滑动窗口算法尝试获取令牌
     *
     * @param key 限流键
     * @param maxRequests 窗口内最大请求数
     * @param windowSizeInMillis 窗口大小（毫秒）
     * @return 是否获取成功
     */
    boolean tryAcquireWithLua(String key, int maxRequests, int windowSizeInMillis);

    /**
     * 使用漏桶算法尝试获取令牌
     *
     * @param key 限流键
     * @param capacity 漏桶容量
     * @param rate 漏水速率（每秒）
     * @return 是否获取成功
     */
    boolean tryAcquireWithLeakyBucket(String key, int capacity, double rate);

    /**
     * 使用计数器算法尝试获取令牌
     *
     * @param key 限流键
     * @param limit 限制次数
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    boolean tryAcquireWithCounter(String key, int limit, int expireTime);

    /**
     * 使用分布式并发限流算法尝试获取令牌
     *
     * @param key 限流键
     * @param maxConcurrent 最大并发数
     * @param id 请求唯一标识
     * @param timeout 过期时间（秒）
     * @return 是否获取成功
     */
    boolean tryAcquireWithConcurrent(String key, int maxConcurrent, String id, int timeout);

    /**
     * 使用带权重的令牌桶算法尝试获取令牌
     *
     * @param key 限流键
     * @param maxTokens 令牌桶容量
     * @param tokensPerSecond 每秒生成的令牌数
     * @param weight 请求权重
     * @return 是否获取成功
     */
    boolean tryAcquireWithWeight(String key, int maxTokens, int tokensPerSecond, int weight);

    /**
     * 初始化限流器
     */
    void init();

    /**
     * 关闭限流器
     */
    void shutdown();


} 