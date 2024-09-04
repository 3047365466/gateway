package com.edan.rapid.core.netty.processor.filter;

/**
 * <B>主类名称：</B>ProcessorFilter<BR>
 * <B>概要说明：</B>执行过滤器的接口操作<BR>
 * @author JiFeng
 * @since 2021年12月14日 上午12:41:20
 */
public interface ProcessorFilter<T> {

	/**
	 * <B>方法名称：</B>check<BR>
	 * <B>概要说明：</B>过滤器是否执行的校验方法<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:42:35
	 * @param t
	 * @return
	 * @throws Exception
	 */
	boolean check(T t) throws Throwable;
	
	/**
	 * <B>方法名称：</B>entry<BR>
	 * <B>概要说明：</B>真正执行过滤器的方法<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:43:31
	 * @param t
	 * @param args
	 * @throws Throwable
	 */
	void entry(T t, Object... args) throws Throwable;
	
	/**
	 * <B>方法名称：</B>fireNext<BR>
	 * <B>概要说明：</B>触发下一个过滤器执行<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:44:10
	 * @param t
	 * @param args
	 * @throws Throwable
	 */
	void fireNext(T t, Object... args) throws Throwable;
	
	/**
	 * <B>方法名称：</B>transformEntry<BR>
	 * <B>概要说明：</B>对象传输的方法<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:46:36
	 * @param t
	 * @param args
	 * @throws Throwable
	 */
	void transformEntry(T t, Object... args) throws Throwable;
		
	
	/**
	 * <B>方法名称：</B>init<BR>
	 * <B>概要说明：</B>过滤器初始化的方法，如果子类有需求则进行覆盖<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:44:57
	 * @throws Exception
	 */
	default void init() throws Exception {
		
	}
	
	/**
	 * <B>方法名称：</B>destroy<BR>
	 * <B>概要说明：</B>过滤器销毁的方法，如果子类有需求则进行覆盖<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:44:57
	 * @throws Exception
	 */
	default void destroy() throws Exception {
		
	}
	
	/**
	 * <B>方法名称：</B>refresh<BR>
	 * <B>概要说明：</B>过滤器刷新的方法，如果子类有需求则进行覆盖<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午12:44:57
	 * @throws Exception
	 */
	default void refresh() throws Exception {
		
	}
	
}
