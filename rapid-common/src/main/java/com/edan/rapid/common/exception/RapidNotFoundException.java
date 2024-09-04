package com.edan.rapid.common.exception;

import com.edan.rapid.common.enums.ResponseCode;

/**
 * <B>主类名称：</B>RapidNotFoundException<BR>
 * <B>概要说明：</B>服务信息未找到异常定义：比如服务定义、实例等信息未找到均会抛出此异常<BR>
 * @author JiFeng
 * @since 2021年12月11日 上午1:23:06
 */
public class RapidNotFoundException extends RapidBaseException {

	private static final long serialVersionUID = -5534700534739261761L;

	public RapidNotFoundException(ResponseCode code) {
		super(code.getMessage(), code);
	}
	
	public RapidNotFoundException(Throwable cause, ResponseCode code) {
		super(code.getMessage(), cause, code);
	}
	
}
