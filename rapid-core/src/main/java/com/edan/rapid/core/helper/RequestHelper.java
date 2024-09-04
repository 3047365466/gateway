package com.edan.rapid.core.helper;

import com.edan.rapid.common.config.DynamicConfigManager;
import com.edan.rapid.common.config.Rule;
import com.edan.rapid.common.config.ServiceDefinition;
import com.edan.rapid.common.config.ServiceInvoker;
import com.edan.rapid.common.constants.BasicConst;
import com.edan.rapid.common.constants.RapidConst;
import com.edan.rapid.common.constants.RapidProtocol;
import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.common.exception.RapidNotFoundException;
import com.edan.rapid.common.exception.RapidPathNoMatchedException;
import com.edan.rapid.common.exception.RapidResponseException;
import com.edan.rapid.common.util.AntPathMatcher;
import com.edan.rapid.core.context.AttributeKey;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.context.RapidRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <B>主类名称：</B>RequestHelper<BR>
 * <B>概要说明：</B>解析请求信息，构建上下文对象<BR>
 * @author JiFeng
 * @since 2021年12月10日 下午10:57:23
 */
public class RequestHelper {
	
	private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

	/**
	 * <B>方法名称：</B>doContext<BR>
	 * <B>概要说明：</B>解析FullHttpRequest 构建RapidContext核心构建方法<BR>
	 * @author JiFeng
	 * @since 2021年12月10日 下午10:58:53
	 * @param request
	 * @param ctx
	 * @return RapidContext
	 */
	public static RapidContext doContext(FullHttpRequest request, ChannelHandlerContext ctx) {
		
		//	1. 	构建请求对象RapidRequest
		RapidRequest rapidRequest = doRequest(request, ctx);
		
		//	2.	根据请求对象里的uniqueId，获取资源服务信息(也就是服务定义信息)
		ServiceDefinition serviceDefinition = getServiceDefinition(rapidRequest);
		
		//	3.	快速路径匹配失败的策略
		if(!ANT_PATH_MATCHER.match(serviceDefinition.getPatternPath(), rapidRequest.getPath())) {
			throw new RapidPathNoMatchedException();
		}
		
		//	4. 	根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
		ServiceInvoker serviceInvoker = getServiceInvoker(rapidRequest, serviceDefinition);
		String ruleId = serviceInvoker.getRuleId();
		Rule rule = DynamicConfigManager.getInstance().getRule(ruleId);
		
		//	5. 	构建我们而定RapidContext对象
		RapidContext rapidContext = new RapidContext.Builder()
				.setProtocol(serviceDefinition.getProtocol())
				.setRapidRequest(rapidRequest)
				.setNettyCtx(ctx)
				.setKeepAlive(HttpUtil.isKeepAlive(request))
				.setRule(rule)
				.build();
		
		//	6. 	设置一些必要的上下文参数用于后面使用
		putContext(rapidContext, serviceInvoker);
		
		return rapidContext;
	}
	
	/**
	 * <B>方法名称：</B>doRequest<BR>
	 * <B>概要说明：</B>构建RapidRequest请求对象<BR>
	 * @author JiFeng
	 * @since 2021年12月10日 下午11:07:40
	 * @param fullHttpRequest
	 * @param ctx
	 * @return RapidRequest
	 */
	private static RapidRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
		
		HttpHeaders headers = fullHttpRequest.headers();
		//	从header头获取必须要传入的关键属性 uniqueId
		String uniqueId = headers.get(RapidConst.UNIQUE_ID);
		
		if(StringUtils.isBlank(uniqueId)) {
			throw new RapidResponseException(ResponseCode.REQUEST_PARSE_ERROR_NO_UNIQUEID);
		}
		
		String host = headers.get(HttpHeaderNames.HOST);
		HttpMethod method = fullHttpRequest.method();
		String uri = fullHttpRequest.uri();
		String clientIp = getClientIp(ctx, fullHttpRequest);
		String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
		Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);		
			
		RapidRequest rapidRequest = new RapidRequest(uniqueId,
				charset,
				clientIp,
				host, 
				uri, 
				method,
				contentType,
				headers,
				fullHttpRequest);
		
		return rapidRequest;
	}
	
	/**
	 * <B>方法名称：</B>getClientIp<BR>
	 * <B>概要说明：</B>获取客户端ip<BR>
	 * @author JiFeng
	 * @since 2021年12月10日 下午11:19:21
	 * @param ctx
	 * @param request
	 * @return ClientIp
	 */
	private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
		String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);
		
		String clientIp = null;
		if(StringUtils.isNotEmpty(xForwardedValue)) {
			List<String> values = Arrays.asList(xForwardedValue.split(", "));
			if(values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
				clientIp = values.get(0);
			}
		}
		if(clientIp == null) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			clientIp = inetSocketAddress.getAddress().getHostAddress();
		}
		return clientIp;
	}

	/**
	 * <B>方法名称：</B>getServiceDefinition<BR>
	 * <B>概要说明：</B>通过请求对象获取服务资源信息<BR>
	 * @author JiFeng
	 * @since 2021年12月11日 上午12:58:05
	 * @param rapidRequest
	 * @return ServiceDefinition
	 */
	private static ServiceDefinition getServiceDefinition(RapidRequest rapidRequest) {
		//	ServiceDefinition从哪里获取，就是在网关服务初始化的时候(加载的时候)？ 从缓存信息里获取
		ServiceDefinition serviceDefinition = DynamicConfigManager.getInstance().getServiceDefinition(rapidRequest.getUniqueId());
		//	做异常情况判断
		if(serviceDefinition == null) {
			throw new RapidNotFoundException(ResponseCode.SERVICE_DEFINITION_NOT_FOUND);
		}
		return serviceDefinition;
	}

	/**
	 * <B>方法名称：</B>getServiceInvoker<BR>
	 * <B>概要说明：</B>根据请求对象和服务定义对象获取对应的ServiceInvoke<BR>
	 * @author JiFeng
	 * @since 2021年12月13日 下午10:57:51
	 * @param rapidRequest
	 * @param serviceDefinition
	 */
	private static ServiceInvoker getServiceInvoker(RapidRequest rapidRequest, ServiceDefinition serviceDefinition) {
		Map<String, ServiceInvoker> invokerMap = serviceDefinition.getInvokerMap();
		ServiceInvoker serviceInvoker = invokerMap.get(rapidRequest.getPath());
		if(serviceInvoker == null) {
			throw new RapidNotFoundException(ResponseCode.SERVICE_INVOKER_NOT_FOUND);
		}
		return serviceInvoker;
	}

	/**
	 * <B>方法名称：</B>putContext<BR>
	 * <B>概要说明：</B>设置必要的上下文方法<BR>
	 * @author JiFeng
	 * @since 2021年12月13日 下午11:04:34
	 * @param rapidContext
	 * @param serviceInvoker
	 */
	private static void putContext(RapidContext rapidContext, ServiceInvoker serviceInvoker) {
		switch (rapidContext.getProtocol()) {
			case RapidProtocol.HTTP:
				rapidContext.putAttribute(AttributeKey.HTTP_INVOKER, serviceInvoker);
				break;
			case RapidProtocol.DUBBO:
				rapidContext.putAttribute(AttributeKey.DUBBO_INVOKER, serviceInvoker);
				break;
			default:
				break;
		}
	}

	
}
