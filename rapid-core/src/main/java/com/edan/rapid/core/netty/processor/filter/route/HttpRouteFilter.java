package com.edan.rapid.core.netty.processor.filter.route;

import com.edan.rapid.common.constants.ProcessorFilterConstants;
import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.common.exception.RapidConnectException;
import com.edan.rapid.common.exception.RapidResponseException;
import com.edan.rapid.common.util.TimeUtil;
import com.edan.rapid.core.RapidConfigLoader;
import com.edan.rapid.core.context.Context;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.context.RapidResponse;
import com.edan.rapid.core.helper.AsyncHttpHelper;
import com.edan.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.edan.rapid.core.netty.processor.filter.Filter;
import com.edan.rapid.core.netty.processor.filter.FilterConfig;
import com.edan.rapid.core.netty.processor.filter.ProcessorFilterType;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * <B>主类名称：</B>HttpRouteFilter<BR>
 * <B>概要说明：</B>请求路由的中置过滤器<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午4:54:36
 */
@Filter(
		id = ProcessorFilterConstants.HTTP_ROUTE_FILTER_ID,
		name = ProcessorFilterConstants.HTTP_ROUTE_FILTER_NAME,
		value = ProcessorFilterType.ROUTE,
		order = ProcessorFilterConstants.HTTP_ROUTE_FILTER_ORDER
		)
@Slf4j
public class HttpRouteFilter extends AbstractEntryProcessorFilter<FilterConfig> {

	public HttpRouteFilter() {
		super(FilterConfig.class);
	}

	@Override
	public void entry(Context ctx, Object... args) throws Throwable {
		// 获取请求信息
		RapidContext rapidContext = (RapidContext) ctx;
		Request request = rapidContext.getRequestMutale().build();
		
		//	设置RS:
		rapidContext.setRSTime(TimeUtil.currentTimeMillis());
		
		CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);
		
		//	双异步和单异步模式
		boolean whenComplete = RapidConfigLoader.getRapidConfig().isWhenComplete();
		
		//	单异步模式
		if(whenComplete) {
			future.whenComplete((response, throwable) -> {
				complete(request, response, throwable, rapidContext, args);
			});
		}
		//	双异步模式
		else {
			future.whenCompleteAsync((response, throwable) -> {
				complete(request, response, throwable, rapidContext, args);
			});					
		}
	}

	/**
	 * <B>方法名称：</B>complete<BR>
	 * <B>概要说明：</B>真正执行请求响应回来的操作方法<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午5:02:06
	 * @param request
	 * @param response
	 * @param throwable
	 * @param rapidContext
	 * @param args
	 */
	private void complete(Request request, 
			Response response,
			Throwable throwable,
			RapidContext rapidContext,
			Object... args) {
		try {
			//	设置RR:
			rapidContext.setRRTime(TimeUtil.currentTimeMillis());
			
			//	1. 释放请求资源
			rapidContext.releaseRequest();
			//	2. 判断是否有异常产生
			if(Objects.nonNull(throwable)) {
				String url = request.getUrl();
				//	超时异常
				if(throwable instanceof java.util.concurrent.TimeoutException) {
					log.warn("#HttpRouteFilter# complete返回响应执行， 请求路径：{}，耗时超过 {}  ms.",
							url, 
							(request.getRequestTimeout() == 0 ? 
									RapidConfigLoader.getRapidConfig().getHttpRequestTimeout() :
									request.getRequestTimeout())
							);
					//	网关里设置异常都是使用自定义异常
					rapidContext.setThrowable(new RapidResponseException(ResponseCode.REQUEST_TIMEOUT));
				} 
				//	其他异常情况
				else {
					rapidContext.setThrowable(new RapidConnectException(throwable, 
							rapidContext.getUniqueId(),
							url,
							ResponseCode.HTTP_RESPONSE_ERROR));
				}
			} 
			//	正常返回响应结果：
			else {
				//	设置响应信息
				rapidContext.setResponse(RapidResponse.buildRapidResponse(response));
			}
			
		} catch (Throwable t) {
			//	最终兜底异常处理
			rapidContext.setThrowable(new RapidResponseException(ResponseCode.INTERNAL_ERROR));
			log.error("#HttpRouteFilter# complete catch到未知异常", t);
		} finally {
			try {
				//	1.	设置写回标记
				rapidContext.writtened();
				
				//	2. 	让异步线程内部自己进行触发下一个节点执行
				super.fireNext(rapidContext, args);
			} catch (Throwable t) {
				//	兜底处理，把异常信息放入上下文
				rapidContext.setThrowable(new RapidResponseException(ResponseCode.INTERNAL_ERROR));
				log.error("#HttpRouteFilter# fireNext出现异常", t);
			}
		}
	}

}
