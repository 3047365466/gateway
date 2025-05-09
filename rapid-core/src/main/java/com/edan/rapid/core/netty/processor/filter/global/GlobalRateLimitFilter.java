package com.edan.rapid.core.netty.processor.filter.global;

import com.edan.rapid.common.constants.ProcessorFilterConstants;
import com.edan.rapid.common.exception.RapidResponseException;
import com.edan.rapid.core.RapidConfig;
import com.edan.rapid.core.RapidConfigLoader;
import com.edan.rapid.core.context.Context;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.limiting.RateLimiterConfig;
import com.edan.rapid.core.limiting.RateLimiterFactory;
import com.edan.rapid.core.limiting.RateLimiterService;
import com.edan.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.edan.rapid.core.netty.processor.filter.Filter;
import com.edan.rapid.core.netty.processor.filter.FilterConfig;
import com.edan.rapid.core.netty.processor.filter.ProcessorFilterType;
import lombok.extern.slf4j.Slf4j;

import static com.edan.rapid.common.enums.ResponseCode.RATE_LIMITED;

/**
 * 全局限流过滤器
 * 从全局配置中获取限流设置，无需使用者手动传递配置
 */
@Filter(
        id = ProcessorFilterConstants.LIMIT_GLOBAL_FILTER_ID,
        name = ProcessorFilterConstants.LIMIT_GLOBAL_FILTER_NAME,
        value = ProcessorFilterType.GLOBAL,
        order = ProcessorFilterConstants.LIMIT_GLOBAL_FILTER_ORDER
)
@Slf4j
public class GlobalRateLimitFilter extends AbstractEntryProcessorFilter<FilterConfig> {

    /**
     * 限流服务实例
     */
    public static RateLimiterService rateLimiterService;

    /**
     * 限流器是否就绪
     */
    public static volatile boolean limiterReady = false;

    /**
     * 过滤器配置
     */
    private RateLimiterConfig filterConfig;

    public GlobalRateLimitFilter() {
        super(FilterConfig.class);
    }

    @Override
    public void init() {
        // 从RapidConfig中获取全局限流配置
        RapidConfig rapidConfig = RapidConfigLoader.getRapidConfig();
        
        // 创建过滤器配置
        this.filterConfig = RateLimiterConfig.builder()
                .enabled(rapidConfig.isLimitEnabled())
                .keyPrefix(rapidConfig.getLimitKeyPrefix() != null ? 
                          rapidConfig.getLimitKeyPrefix() : "rate_limit:")
                .algorithm(RateLimiterConfig.convertGlobalLimitType(rapidConfig.getLimitAlgorithm()))
                .dimension(RateLimiterConfig.LimitDimension.GLOBAL)
                .maxTokens(rapidConfig.getLimitMaxTokens())
                .tokensPerSecond(rapidConfig.getLimitTokensPerSecond())
                .maxRequests(rapidConfig.getLimitMaxRequests())
                .windowSize(rapidConfig.getLimitWindowSize())
                .limitMessage(rapidConfig.getLimitMessage() != null ?
                             rapidConfig.getLimitMessage() : "Too many requests, please try again later")
                .build();

        if (!filterConfig.isEnabled()) {
            log.info("Global rate limiting is disabled by configuration");
            return;
        }

        // 获取限流器类型
        String limiterType = rapidConfig.getRateLimiterType();

        // 创建限流器实例
        try {
            rateLimiterService = createRateLimiterService(limiterType, filterConfig);
//            rateLimiterService.init();
            limiterReady = true;

            log.info("GlobalLimitFilter initialized with limiter type: {} and algorithm: {}",
                    limiterType, filterConfig.getAlgorithm());
        } catch (Exception e) {
            log.error("Failed to initialize rate limiter: " + e.getMessage(), e);
            // 初始化失败时，不阻止过滤器链继续执行，但限流功能不可用
        }
    }



    /**
     * 转换全局配置中的限流维度到过滤器限流维度
     */


    /**
     * 创建适当的限流器服务
     */
    public static RateLimiterService createRateLimiterService(String limiterType, RateLimiterConfig config) {
        RateLimiterFactory.LimiterType type;
        try {
            type = RateLimiterFactory.LimiterType.valueOf(limiterType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid limiter type: {}, falling back to LOCAL", limiterType);
            type = RateLimiterFactory.LimiterType.LOCAL;
        }

        return RateLimiterFactory.createLimiter(type, config);
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        RapidContext context = (RapidContext) ctx;

        if (!filterConfig.isEnabled() || !limiterReady || rateLimiterService == null) {
            // 限流器未启用或未就绪，直接放行
            super.fireNext(ctx, args);
            return;
        }

        // 获取限流的key
        String limitKey = RateLimiterConfig.getLimitKey(context, filterConfig.getDimension(), filterConfig.getKeyPrefix());

        boolean allowed;
        try {
            allowed = rateLimiterService.tryAcquire(limitKey);
            if (!allowed) {
                // 被限流，返回限流响应
                log.warn("Global Rate limited: {} - {}", limitKey, context.getRequest().getPath());
                throw new RapidResponseException(RATE_LIMITED);
            }
        } catch (Exception e) {
            log.warn("Error during rate limiting, allowing request: " + e.getMessage());
            // 限流出错时，默认放行以不影响业务
            allowed = true;
        } finally {
            super.fireNext(ctx, args);
        }


    }



    @Override
    public void destroy() {
        if (rateLimiterService != null) {
            try {
                rateLimiterService.shutdown();
                log.info("GlobalLimitFilter shutdown");
            } catch (Exception e) {
                log.error("Error during rate limiter shutdown: " + e.getMessage(), e);
            } finally {
                rateLimiterService = null;
                limiterReady = false;
            }
        }
    }

    @Override
    public boolean check(Context ctx) throws Throwable {
        return true;
    }
}