package com.edan.rapid.client.core.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * <B>主类名称：</B>RapidProperties<BR>
 * <B>概要说明：</B>配置类<BR>
 * @author JiFeng
 * @since 2021年12月18日 上午1:38:51
 */
@Data
@ConfigurationProperties(prefix = RapidProperties.RAPID_PREFIX)
public class RapidProperties {

	public static final String RAPID_PREFIX = "rapid";
	
	/**
	 * 	etcd注册中心地址
	 */
	private String registryAddress;
	
	/**
	 * 	etcd注册命名空间
	 */
	private String namespace = RAPID_PREFIX;
	
	/**
	 * 	环境属性
	 */
	private String env = "dev";
	
	
}
