package com.edan.rapid.core.netty.processor;

import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.common.exception.RapidNotFoundException;
import com.edan.rapid.common.exception.RapidPathNoMatchedException;
import com.edan.rapid.common.exception.RapidResponseException;
import com.edan.rapid.core.context.HttpRequestWrapper;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.helper.RequestHelper;
import com.edan.rapid.core.helper.ResponseHelper;
import com.edan.rapid.core.netty.processor.filter.DefaultProcessorFilterFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <B>主类名称：</B>NettyCoreProcessor<BR>
 * <B>概要说明：</B>核心流程的主执行逻辑<BR>
 * @author edan
 * @since 2021年12月5日 下午9:51:34
 */
@Slf4j
public class NettyCoreProcessor implements NettyProcessor {

	private DefaultProcessorFilterFactory processorFilterFactory = DefaultProcessorFilterFactory.getInstance();
	@Override
	public void process(HttpRequestWrapper event) {
		FullHttpRequest request = event.getFullHttpRequest();
		ChannelHandlerContext ctx = event.getCtx();
		try {
			//	1. 解析FullHttpRequest, 把他转换为我们自己想要的内部对象：Context
			RapidContext rapidContext = RequestHelper.doContext(request, ctx);

			//	2. 执行整个的过滤器逻辑：FilterChain
			processorFilterFactory.doFilterChain(rapidContext);

		} catch (RapidPathNoMatchedException e) {
			log.error("#NettyCoreProcessor# process 网关资指定路径为匹配异常，快速失败： code: {}, msg: {}",
					e.getCode().getCode(), e.getCode().getMessage(), e);
			FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
			//	释放资源写回响应
			doWriteAndRelease(ctx, request, response);
		} catch(RapidNotFoundException e) {
			log.error("#NettyCoreProcessor# process 网关资源未找到异常： code: {}, msg: {}",
					e.getCode().getCode(), e.getCode().getMessage(), e);
			FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
			//	释放资源写回响应
			doWriteAndRelease(ctx, request, response);

		} catch(RapidResponseException e) {
			log.error("#NettyCoreProcessor# process 网关内部未知错误异常： code: {}, msg: {}",
					e.getCode().getCode(), e.getCode().getMessage(), e);
			FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
			//	释放资源写回响应
			doWriteAndRelease(ctx, request, response);

		} catch (Throwable t) {
			log.error("#NettyCoreProcessor# process 网关内部未知错误异常", t);
			FullHttpResponse response = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
			//	释放资源写回响应
			doWriteAndRelease(ctx, request, response);
		}
	}

	/**
	 * <B>方法名称：</B>doWriteAndRelease<BR>
	 * <B>概要说明：</B>写回响应信息并释放资源<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午6:12:10
	 * @param ctx
	 * @param request
	 * @param response
	 */
	private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		boolean release = ReferenceCountUtil.release(request);
		if(!release) {
			log.warn("#NettyCoreProcessor# doWriteAndRelease release fail 释放资源失败， request:{}, release:{}",
					request.uri(),
					release);
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
