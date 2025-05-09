package com.edan.rapid.core.netty.processor.filter.pre;

import com.edan.rapid.common.constants.ProcessorFilterConstants;
import com.edan.rapid.common.exception.RapidResponseException;
import com.edan.rapid.core.context.Context;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.limiting.RateLimiterConfig;
import com.edan.rapid.core.limiting.RateLimiterService;
import com.edan.rapid.core.limiting.RedisRateLimiterService;
import com.edan.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.edan.rapid.core.netty.processor.filter.Filter;
import com.edan.rapid.core.netty.processor.filter.FilterConfig;
import com.edan.rapid.core.netty.processor.filter.ProcessorFilterType;
import com.edan.rapid.core.netty.processor.filter.global.GlobalRateLimitFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edan.rapid.common.enums.ResponseCode.RATE_LIMITED;

/**
 * <B>主类名称：</B>LoadBalancePreFilter<BR>
 * <B>概要说明：</B>负载均衡前置过滤器<BR>
 * @author edan
 * @since 2024年8月20日 下午4:18:17
 */
@Slf4j
@Filter(
		id = ProcessorFilterConstants.LIMIT_PRE_FILTER_ID,
		name = ProcessorFilterConstants.LIMIT_PRE_FILTER_NAME,
		value = ProcessorFilterType.PRE,
		order = ProcessorFilterConstants.LIMIT_PRE_FILTER_ORDER
		)
public class RateLimitPreFilter extends AbstractEntryProcessorFilter<RateLimitPreFilter.Config> {

	/**
	 * 限流服务实例
	 */
	private RateLimiterService rateLimiterService;

	public RateLimitPreFilter() {
		super(Config.class);
	}

	@Override
	public void init() throws Exception {
		if (!GlobalRateLimitFilter.limiterReady) {
			try {
				this.rateLimiterService = new RedisRateLimiterService();
			} catch (Exception e) {
				throw new RuntimeException("RateLimitPreFilter init is filed !");
			}
		} else {
			this.rateLimiterService = GlobalRateLimitFilter.rateLimiterService;
		}
	}

	@Override
	public void entry(Context ctx, Object... args) throws Throwable {
		try {
			RapidContext rapidContext = (RapidContext) ctx;
			Config config = (Config) args[0];
			config.getRateLimitConfig().forEach((k, v) -> {
				RateLimiterConfig.LimitDimension dimension = RateLimiterConfig.convertGlobalLimitDimension(k);
				String limitKey = RateLimiterConfig.getLimitKey(rapidContext, dimension, config.keyPrefix);
				// 自定义限流
				if (!limitKey.isEmpty()) {
					v.keys = Collections.singletonList(limitKey);
				}
				if (!rateLimiterService.tryAcquireByPreFilter(v)) {
					// 被限流，返回限流响应
					log.warn("Rate limited: {} - {}", limitKey, rapidContext.getRequest().getPath());
					throw new RapidResponseException(RATE_LIMITED);
				}
			});
		} finally {
			super.fireNext(ctx, args);;
		}
	}
	

	@Override
	public void transformEntry(Context ctx, Object... args) throws Throwable {
		super.transformEntry(ctx, args);
	}

	/**
	 * <B>主类名称：</B>Config<BR>
	 * <B>概要说明：</B>限流前置过滤器配置<BR>
	 * @author edan
	 * @since 2024年8月20日 下午4:21:54
	 */
	@Getter
	@Setter
	public static class Config extends FilterConfig {

		private String keyPrefix = "rate_limit:";

		private Map<String, LuaParameter> rateLimitConfig = new HashMap<>();

		@Getter
		@Setter
		public static class LuaParameter {
			// 自定义限流key，绑定自定义脚本
			List<String> keys;
			List<String> values;
			// 算法选择
			String algorithm;
			// 限流范围选择
			String dimension;
			// 用户自定义脚本
			String lua;
		}
	}

}

