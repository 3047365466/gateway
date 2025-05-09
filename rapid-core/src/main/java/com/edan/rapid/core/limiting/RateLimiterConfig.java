package com.edan.rapid.core.limiting;

import com.edan.rapid.core.context.RapidContext;
import lombok.Builder;
import lombok.Data;

/**
 * 限流器配置
 */
@Data
@Builder
public class RateLimiterConfig {

    /**
     * 是否启用全局限流
     */
    private boolean enabled = true;

    /**
     * 限流区域
     */
    private LimitDimension dimension = LimitDimension.GLOBAL;

    /**
     * 限流算法
     */
    private LimitAlgorithm algorithm = LimitAlgorithm.TOKEN_BUCKET;

    
    /**
     * 默认令牌桶容量
     */
    public static final int DEFAULT_MAX_TOKENS = 100;
    
    /**
     * 默认令牌生成速率
     */
    public static final int DEFAULT_TOKENS_PER_SECOND = 10;
    
    /**
     * 默认窗口大小（秒）
     */
    public static final int DEFAULT_WINDOW_SIZE = 60;
    
    /**
     * 默认窗口请求数
     */
    public static final int DEFAULT_MAX_REQUESTS = 100;
    

    
    /**
     * 默认前缀
     */
    private String keyPrefix = "rate_limit:";

    
    /**
     * 令牌桶容量
     */
    private int maxTokens = DEFAULT_MAX_TOKENS;
    
    /**
     * 每秒产生的令牌数
     */
    private int tokensPerSecond = DEFAULT_TOKENS_PER_SECOND;
    
    /**
     * 窗口内最大请求数
     */
    private int maxRequests = DEFAULT_MAX_REQUESTS;
    
    /**
     * 窗口大小（秒）
     */
    private int windowSize = DEFAULT_WINDOW_SIZE;
    
    /**
     * 限流消息
     */
    private String limitMessage;
    
    /**
     * 限流算法类型
     */
    public enum LimitAlgorithm {
        /**
         * 令牌桶算法
         */
        TOKEN_BUCKET,

        /**
         * 固定窗口算法
         */
        FIXED_WINDOW,

        /**
         * 滑动窗口算法
         */
        SLIDING_WINDOW,
        
        /**
         * 漏桶算法
         */
        LEAKY_BUCKET,
        
        /**
         * 计数器算法
         */
        COUNTER,
        
        /**
         * 并发限流算法
         */
        CONCURRENT,
        
        /**
         * 带权重的令牌桶算法
         */
        WEIGHTED_TOKEN_BUCKET
    }

    /**
     * 限流维度枚举
     */
    public enum LimitDimension {
        /**
         * 全局限流
         */
        GLOBAL,
        /**
         * 按服务限流
         */
        SERVICE,

        /**
         * 按路径限流
         */
        PATH,

        /**
         * 按IP限流
         */
        IP,

        /**
         * 自定义， 通过保存在请求的header头中的信息进行限流
         */
        CUSTOM
    }
    
    // 兼容方法
    public LimitDimension getLimitDimension() {
        return dimension;
    }
    
    public LimitAlgorithm getLimitAlgorithm() {
        return algorithm;
    }

    public static RateLimiterConfig defaultConfig() {
        return RateLimiterConfig.builder().build();
    }

    /**
     * 根据配置获取限流键
     */
    public static String getLimitKey(RapidContext ctx, LimitDimension dimension, String keyPrefix) {
        // 根据不同的限流维度构建key
        switch (dimension) {
            case IP:
                return keyPrefix + (ctx.getClientIp() != null ? ctx.getClientIp() : "unknown");
            case SERVICE:
                if (ctx.getUniqueId() != null && ctx.getUniqueId() != null) {
                    return keyPrefix + ctx.getUniqueId();
                }
                return keyPrefix + "unknown-service";
            case PATH:
                return keyPrefix + ctx.getRequest().getPath();
            case CUSTOM:
                return "";
            case GLOBAL:
            default:
                return keyPrefix + "global";
        }
    }
    /**
     * 转换全局配置中的限流区域到过滤器类型
     */
    public static LimitDimension convertGlobalLimitDimension(String dimension) {
        if (dimension == null) {
            return LimitDimension.GLOBAL;
        }

        switch (dimension.toUpperCase()) {
            case "IP":
                return LimitDimension.IP;
            case "SERVICE":
                return LimitDimension.SERVICE;
            case "PATH":
                return LimitDimension.PATH;
            case "CUSTOM":
                return LimitDimension.CUSTOM;
            case "GLOBAL":
            default:
                return LimitDimension.GLOBAL;
        }
    }

    /**
     * 转换全局配置中的限流类型到过滤器限流类型
     */
    public static LimitAlgorithm convertGlobalLimitType(String algorithm) {
        if (algorithm == null) {
            return LimitAlgorithm.TOKEN_BUCKET;
        }

        switch (algorithm.toUpperCase()) {
            case "FIXED_WINDOW":
                return LimitAlgorithm.FIXED_WINDOW;
            case "SLIDING_WINDOW":
                return LimitAlgorithm.SLIDING_WINDOW;
            case "LEAKY_BUCKET":
                return LimitAlgorithm.LEAKY_BUCKET;
            case "COUNTER":
                return LimitAlgorithm.COUNTER;
            case "CONCURRENT":
                return LimitAlgorithm.CONCURRENT;
            case "WEIGHTED_TOKEN_BUCKET":
                return LimitAlgorithm.WEIGHTED_TOKEN_BUCKET;
            case "TOKEN_BUCKET":
            default:
                return LimitAlgorithm.TOKEN_BUCKET;
        }
    }
} 