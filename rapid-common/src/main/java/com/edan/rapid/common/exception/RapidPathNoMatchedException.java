package com.edan.rapid.common.exception;

import com.edan.rapid.common.enums.ResponseCode;

/**
 * <B>主类名称：</B>RapidPathNoMatchedException<BR>
 * <B>概要说明：</B>请求路径不匹配的异常定义类<BR>
 * @author JiFeng
 * @since 2021年12月13日 下午10:48:59
 */
public class RapidPathNoMatchedException extends RapidBaseException {

	private static final long serialVersionUID = -6695383751311763169L;

	
	public RapidPathNoMatchedException() {
		this(ResponseCode.PATH_NO_MATCHED);
	}
	
	public RapidPathNoMatchedException(ResponseCode code) {
		super(code.getMessage(), code);
	}
	
	public RapidPathNoMatchedException(Throwable cause, ResponseCode code) {
		super(code.getMessage(), cause, code);
	}
}
