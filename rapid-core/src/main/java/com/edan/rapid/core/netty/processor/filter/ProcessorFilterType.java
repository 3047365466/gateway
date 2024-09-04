package com.edan.rapid.core.netty.processor.filter;

/**
 * <B>主类名称：</B>ProcessorFilterType<BR>
 * <B>概要说明：</B>过滤器的类型定义<BR>
 * @author JiFeng
 * @since 2021年12月14日 上午12:50:11
 */
public enum ProcessorFilterType {

	PRE("PRE", "前置过滤器"),
	
	ROUTE("ROUTE", "中置过滤器"),
	
	ERROR("ERROR", "前置过滤器"),
	
	POST("POST", "前置过滤器");
	
	private final String code ;
	
	private final String message;
	
	ProcessorFilterType(String code, String message){
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	
}
