package com.edan.rapid.common.exception;

import com.edan.rapid.common.enums.ResponseCode;
import lombok.Getter;

/**
 * <B>主类名称：</B>RapidConnectException<BR>
 * <B>概要说明：</B>连接异常定义类<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午5:17:05
 */
public class RapidConnectException extends RapidBaseException {

	private static final long serialVersionUID = -8503239867913964958L;

	@Getter
	private final String uniqueId;
	
	@Getter
	private final String requestUrl;
	
	public RapidConnectException(String uniqueId, String requestUrl) {
		this.uniqueId = uniqueId;
		this.requestUrl = requestUrl;
	}
	
	public RapidConnectException(Throwable cause, String uniqueId, String requestUrl, ResponseCode code) {
		super(code.getMessage(), cause, code);
		this.uniqueId = uniqueId;
		this.requestUrl = requestUrl;
	}

}
