package com.edan.rapid.core.context;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.common.util.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

/**
 * <B>主类名称：</B>RapidResponse<BR>
 * <B>概要说明：</B>网关响应封装类<BR>
 * @author JiFeng
 * @since 2021年12月9日 下午1:33:13
 */
@Data
public class RapidResponse {

	//	响应头
	private HttpHeaders responseHeaders = new DefaultHttpHeaders();
	
	//	额外的响应结果
	private final HttpHeaders extraResponseHeaders = new DefaultHttpHeaders(); 
	
	//	返回的响应内容
	private String content;
	
	//	返回响应状态码
	private HttpResponseStatus httpResponseStatus;
	
	//	响应对象
	private Response futureResponse;
	
	private RapidResponse() {
	}

	/**
	 * <B>方法名称：</B>putHeader<BR>
	 * <B>概要说明：</B>设置响应头信息<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 下午1:36:39
	 * @param key
	 * @param val
	 */
	public void putHeader(CharSequence key, CharSequence val) {
		responseHeaders.add(key, val);
	}
	
	/**
	 * <B>方法名称：</B>buildRapidResponse<BR>
	 * <B>概要说明：</B>构建网关响应对象<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 下午1:40:27
	 * @param org.asynchttpclient.Response futureResponse
	 * @return RapidResponse
	 */
	public static RapidResponse buildRapidResponse(Response futureResponse) {
		RapidResponse rapidResponse = new RapidResponse();
		rapidResponse.setFutureResponse(futureResponse);
		rapidResponse.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
		return rapidResponse;
	}
	
	/**
	 * <B>方法名称：</B>buildRapidResponse<BR>
	 * <B>概要说明：</B>返回一个json类型的响应信息，失败时候使用<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 下午1:49:02
	 * @param code
	 * @param args
	 * @return RapidResponse
	 */
	public static RapidResponse buildRapidResponse(ResponseCode code, Object... args) {
		ObjectNode objectNode = JSONUtil.createObjectNode();
		objectNode.put(JSONUtil.STATUS, code.getStatus().code());
		objectNode.put(JSONUtil.CODE, code.getCode());
		objectNode.put(JSONUtil.MESSAGE, code.getMessage());
		RapidResponse rapidResponse = new RapidResponse();
		rapidResponse.setHttpResponseStatus(code.getStatus());
		rapidResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, 
				HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
		rapidResponse.setContent(JSONUtil.toJSONString(objectNode));
		return rapidResponse;
	}
	
	/**
	 * <B>方法名称：</B>buildRapidResponseObj<BR>
	 * <B>概要说明：</B>返回一个json类型的响应信息, 成功时候使用<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 下午1:55:36
	 * @param data
	 * @return
	 */
	public static RapidResponse buildRapidResponseObj(Object data) {
		ObjectNode objectNode = JSONUtil.createObjectNode();
		objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
		objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
		objectNode.putPOJO(JSONUtil.DATA, data);
		RapidResponse rapidResponse = new RapidResponse();
		rapidResponse.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
		rapidResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, 
				HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
		rapidResponse.setContent(JSONUtil.toJSONString(objectNode));
		return rapidResponse;
	}
	
	
}
