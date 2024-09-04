package com.edan.rapid.core.netty.processor.filter;

import java.lang.annotation.*;

/**
 * <B>主类名称：</B>Filter<BR>
 * <B>概要说明：</B>过滤器注解类<BR>
 * @author JiFeng
 * @since 2021年12月14日 上午12:54:21
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Filter {

	/**
	 * <B>方法名称：</B>id<BR>
	 * <B>概要说明：</B>过滤器的唯一ID, 必填<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:54:43
	 * @return
	 */
	String id();
	
	/**
	 * <B>方法名称：</B>name<BR>
	 * <B>概要说明：</B>过滤器的名字<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:55:02
	 * @return
	 */
	String name() default "";
	
	/**
	 * <B>方法名称：</B>ProcessorFilterType<BR>
	 * <B>概要说明：</B>过滤器的类型<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:55:53
	 * @return ProcessorFilterType
	 */
	ProcessorFilterType value();
	
	/**
	 * <B>方法名称：</B>order<BR>
	 * <B>概要说明：</B>过滤器的排序，按照此排序从小到大依次执行过滤器<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:56:25
	 * @return
	 */
	int order() default 0;
	
	
}
