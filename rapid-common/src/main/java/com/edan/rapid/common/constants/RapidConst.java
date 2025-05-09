package com.edan.rapid.common.constants;

/**
 * <B>主类名称：</B>RapidConst<BR>
 * <B>概要说明：</B>网关常量类：与业务相关<BR>
 * @author edan
 * @since 2024年8月10日 下午11:13:21
 */
public interface RapidConst {

	String RAPID = "rapid";
	
	String UNIQUE_ID = "uniqueId";
	
	String DEFAULT_VERSION = "1.0.0";
	
	String PROTOCOL_KEY = "protocol";

	/**
	 * 	默认的实例权重为100
	 */
	int DEFAULT_WEIGHT = 100;

	/**
	 * 	请求超时时间默认为20s
	 */
	int DEFAULT_REQUEST_TIMEOUT = 20000;
	
	
}
