package com.edan.rapid.core.helper;


import com.edan.rapid.common.constants.BasicConst;
import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.core.context.Context;
import com.edan.rapid.core.context.RapidResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;

import java.util.Objects;

/**
 * <B>主类名称：</B>ResponseHelper<BR>
 * <B>概要说明：</B>响应的辅助类<BR>
 * @author edan
 * @since 2021年12月8日 下午9:53:15
 */
public class ResponseHelper {

	/**
	 * <B>方法名称：</B>getHttpResponse<BR>
	 * <B>概要说明：</B>获取响应对象<BR>
	 * @author edan
	 * @since 2021年12月8日 下午10:01:43
	 * @param responseCode
	 * @return FullHttpResponse
	 */
	public static FullHttpResponse getHttpResponse(ResponseCode responseCode) {
		RapidResponse rapidResponse = RapidResponse.buildRapidResponse(responseCode);
		DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, 
				HttpResponseStatus.INTERNAL_SERVER_ERROR,
				Unpooled.wrappedBuffer(rapidResponse.getContent().getBytes()));
		
		httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
		httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
		return httpResponse;
	}

	private static FullHttpResponse getHttpResponse(Context ctx, RapidResponse rapidResponse) {
		// 构建响应对象
		DefaultFullHttpResponse httpResponse;
		ByteBuf content;
		if (Objects.nonNull(rapidResponse.getFutureResponse())) {
			content = Unpooled.wrappedBuffer(rapidResponse.getFutureResponse().getResponseBodyAsBytes());
		} else if (Objects.nonNull(rapidResponse.getContent())) {
			content = Unpooled.wrappedBuffer(rapidResponse.getContent().getBytes());
		} else {
			content = Unpooled.wrappedBuffer(BasicConst.BLANK_SEPARATOR_1.getBytes());
		}

		if (Objects.isNull(rapidResponse.getFutureResponse())) {
			httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, rapidResponse.getHttpResponseStatus(), content);
			httpResponse.headers().add(rapidResponse.getResponseHeaders());
			httpResponse.headers().add(rapidResponse.getExtraResponseHeaders());
			httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
		} else {
			rapidResponse.getFutureResponse().getHeaders().add(rapidResponse.getExtraResponseHeaders());

			httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.valueOf(rapidResponse.getFutureResponse().getStatusCode()), content);
			httpResponse.headers().add(rapidResponse.getFutureResponse().getHeaders());
		}
		return httpResponse;
	}


	public static void writeResponse(Context ctx) {
		// 释放资源
		ctx.releaseRequest();
		if (ctx.isWrittened()) {
			FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ctx, (RapidResponse) ctx.getResponse());
			if (!ctx.isKeepAlive()) {
				ctx.getNettyCtx().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
			} else {
				httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				ctx.getNettyCtx().writeAndFlush(httpResponse);
			}
			//	2:	设置写回结束状态为： COMPLETED
			ctx.completed();
		} else if (ctx.isCompleted()) {
			ctx.invokeCompletedCallback();
		}
	}
}
