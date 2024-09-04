package com.edan.rapid.common.constants;

/**
 * <B>主类名称：</B>RapidProtocol<BR>
 * <B>概要说明：</B>协议定义类<BR>
 * @author JiFeng
 * @since 2021年12月13日 下午11:08:39
 */
public interface RapidProtocol {
	
	String HTTP = "http";
	
	String DUBBO = "dubbo";
	
	static boolean isHttp(String protocol) {
		return HTTP.equals(protocol);
	}
	
	static boolean isDubbo(String protocol) {
		return DUBBO.equals(protocol);
	}
	
}
