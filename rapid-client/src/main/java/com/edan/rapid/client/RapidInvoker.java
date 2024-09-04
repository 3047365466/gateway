package com.edan.rapid.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <B>主类名称：</B>RapidInvoker<BR>
 * <B>概要说明：</B>必须要在服务的方法上进行强制的声明.<BR>
 * @author JiFeng
 * @since 2021年12月18日 上午12:41:43
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RapidInvoker {

	/**
	 * <B>方法名称：</B>path<BR>
	 * <B>概要说明：</B>访问路径<BR>
	 * @author JiFeng
	 * @since 2021年12月18日 上午12:39:55
	 * @return path
	 */
	String path();
	
}
