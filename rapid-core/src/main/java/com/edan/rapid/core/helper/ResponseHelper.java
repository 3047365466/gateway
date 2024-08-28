package com.edan.rapid.core.helper;


import com.edan.rapid.common.enums.ResponseCode;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

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
		//	TODO: 目前还没有response对象, 我希望自己去创建出一个RapidResponse
		String errorContent = "响应内部错误";
		DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, 
				HttpResponseStatus.INTERNAL_SERVER_ERROR,
				Unpooled.wrappedBuffer(errorContent.getBytes()));
		
		httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
		httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, errorContent.length());
		return httpResponse;
	}
	
	
	
}
