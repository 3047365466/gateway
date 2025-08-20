package com.edan.rapid.core.netty.processor.filter.route;

import com.edan.rapid.common.config.DubboServiceInvoker;
import com.edan.rapid.common.config.ServiceInvoker;
import com.edan.rapid.common.constants.ProcessorFilterConstants;
import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.common.exception.DubboConnectException;
import com.edan.rapid.common.exception.RapidResponseException;
import com.edan.rapid.common.util.FastJsonConvertUtil;
import com.edan.rapid.common.util.TimeUtil;
import com.edan.rapid.core.RapidConfigLoader;
import com.edan.rapid.core.context.*;
import com.edan.rapid.core.helper.DubboReferenceHelper;
import com.edan.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.edan.rapid.core.netty.processor.filter.Filter;
import com.edan.rapid.core.netty.processor.filter.FilterConfig;
import com.edan.rapid.core.netty.processor.filter.ProcessorFilterType;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * <B>主类名称：</B>DubboRouteFilter<BR>
 * <B>概要说明：</B>DubboRouteFilter<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午9:57:03
 */
@Filter(
		id = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ID,
		name = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_NAME,
		value = ProcessorFilterType.ROUTE,
		order = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ORDER
		)
@Slf4j
public class DubboRouteFilter extends AbstractEntryProcessorFilter<FilterConfig> {

	public DubboRouteFilter() {
		super(FilterConfig.class);
	}

	@Override
	public void entry(Context ctx, Object... args) throws Throwable {
		RapidContext rapidContext = (RapidContext)ctx;
		ServiceInvoker serviceInvoker = rapidContext.getRequiredAttribute(AttributeKey.DUBBO_INVOKER);
		DubboServiceInvoker dubboServiceInvoker = (DubboServiceInvoker)serviceInvoker;

		//	请求协议的校验：
		if(!HttpHeaderValues.APPLICATION_JSON.toString().equals(rapidContext.getOriginRequest().getContentType())) {
			//	显示抛出异常 必须要终止执行
			rapidContext.terminated();
			throw new RapidResponseException(ResponseCode.DUBBO_PARAMETER_VALUE_ERROR);
		}

		String body = rapidContext.getOriginRequest().getBody();

		//	这一步的时候就可以是否请求对象
		rapidContext.releaseRequest();

		java.util.List<Object> parameters = null;

		try {
			parameters = FastJsonConvertUtil.convertJSONToArray(body, Object.class);
		} catch (Exception e) {
			//	如果解析异常
			rapidContext.terminated();
			throw new RapidResponseException(ResponseCode.DUBBO_PARAMETER_VALUE_ERROR);
		}

		//	构建dubbo请求对象
		DubboRequest dubboRequest = DubboReferenceHelper.buildDubboRequest(dubboServiceInvoker, parameters.toArray());

		//	设置RS:
		rapidContext.setRSTime(TimeUtil.currentTimeMillis());

		CompletableFuture<Object> future = DubboReferenceHelper.getInstance().$invokeAsync(rapidContext, dubboRequest);

		//	双异步和单异步模式
		boolean whenComplete = RapidConfigLoader.getRapidConfig().isWhenComplete();

		//	单异步模式
		if(whenComplete) {
			future.whenComplete((response, throwable) -> {
				complete(dubboServiceInvoker, response, throwable, rapidContext, args);
			});
		}
		//	双异步模式
		else {
			future.whenCompleteAsync((response, throwable) -> {
				complete(dubboServiceInvoker, response, throwable, rapidContext, args);
			});
		}
	}

	/**
	 * <B>方法名称：</B>complete<BR>
	 * <B>概要说明：</B>回调响应处理实现<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午9:51:59
	 * @param dubboServiceInvoker
	 * @param response
	 * @param throwable
	 * @param rapidContext
	 * @param args
	 */
	private void complete(DubboServiceInvoker dubboServiceInvoker,
			Object response,
			Throwable throwable,
			RapidContext rapidContext,
			Object[] args) {
		try {
			//	设置RR:
			rapidContext.setRRTime(TimeUtil.currentTimeMillis());

			if(Objects.nonNull(throwable)) {
				DubboConnectException dubboConnectException = new DubboConnectException(throwable,
						rapidContext.getUniqueId(),
						rapidContext.getOriginRequest().getPath(),
						dubboServiceInvoker.getInterfaceClass(),
						dubboServiceInvoker.getMethodName(),
						ResponseCode.DUBBO_RESPONSE_ERROR);
				rapidContext.setThrowable(dubboConnectException);
			} else {
				RapidResponse rapidResponse = RapidResponse.buildRapidResponseObj(response);
				rapidContext.setResponse(rapidResponse);
			}

		} catch (Throwable t) {
			//	最终兜底异常处理
			rapidContext.setThrowable(new RapidResponseException(ResponseCode.INTERNAL_ERROR));
			log.error("#DubboRouteFilter# complete catch到未知异常", t);
		} finally {
			try {
				//	1.	设置写回标记
				rapidContext.writtened();
				//	2. 	让异步线程内部自己进行触发下一个节点执行
				super.fireNext(rapidContext, args);
			} catch (Throwable t) {
				//	兜底处理，把异常信息放入上下文
				rapidContext.setThrowable(new RapidResponseException(ResponseCode.INTERNAL_ERROR));
				log.error("#DubboRouteFilter# fireNext出现异常", t);
			}
		}
	}

}
