package com.edan.rapid.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <B>主类名称：</B>RapidService<BR>
 * <B>概要说明：</B>服务定义注解类<BR>
 * @author JiFeng
 * @since 2021年12月18日 上午12:34:52
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RapidService {

	/**
	 * <B>方法名称：</B>serviceId<BR>
	 * <B>概要说明：</B>服务的唯一ID<BR>
	 * @author JiFeng
	 * @since 2021年12月18日 上午12:26:11
	 * @return
	 */
	String serviceId();
	
	/**
	 * <B>方法名称：</B>version<BR>
	 * <B>概要说明：</B>对应服务的版本号<BR>
	 * @author JiFeng
	 * @since 2021年12月18日 上午12:27:11
	 * @return
	 */
	String version() default "1.0.0";
	
	/**
	 * <B>方法名称：</B>protocol<BR>
	 * <B>概要说明：</B>协议类型<BR>
	 * @author JiFeng
	 * @since 2021年12月18日 上午12:31:53
	 * @return
	 */
	RapidProtocol protocol();
	
	/**
	 * <B>方法名称：</B>patternPath<BR>
	 * <B>概要说明：</B>ANT路径匹配表达式配置<BR>
	 * @author JiFeng
	 * @since 2021年12月18日 上午12:33:23
	 * @return
	 */
	String patternPath();
	
}
