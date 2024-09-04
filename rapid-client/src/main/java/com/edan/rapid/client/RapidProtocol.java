package com.edan.rapid.client;

/**
 * <B>主类名称：</B>RapidProtocol<BR>
 * <B>概要说明：</B>表示注册服务的协议枚举类<BR>
 * @author JiFeng
 * @since 2021年12月18日 上午12:29:08
 */
public enum RapidProtocol {

	HTTP("http", "http协议"),
	DUBBO("dubbo", "http协议");
	
	private String code;
	
	private String desc;
	
	RapidProtocol(String code, String desc){
		this.code = code;
		this.desc = desc;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDesc() {
		return desc;
	}
	
}
