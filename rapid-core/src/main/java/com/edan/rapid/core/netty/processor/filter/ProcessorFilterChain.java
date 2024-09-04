package com.edan.rapid.core.netty.processor.filter;

/**
 * <B>主类名称：</B>ProcessorFilterChain<BR>
 * <B>概要说明：</B>链表的抽象接口：添加一些简单的操作方法<BR>
 * @author JiFeng
 * @since 2021年12月14日 上午1:43:18
 */
public abstract class ProcessorFilterChain<T> extends AbstractLinkedProcessorFilter<T> {

	/**
	 * <B>方法名称：</B>addFirst<BR>
	 * <B>概要说明：</B>在链表的头部添加元素<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午1:44:28
	 * @param filter
	 */
	public abstract void addFirst(AbstractLinkedProcessorFilter<T> filter);
	
	/**
	 * <B>方法名称：</B>addLast<BR>
	 * <B>概要说明：</B>在链表的尾部添加元素<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午1:44:33
	 * @param filter
	 */
	public abstract void addLast(AbstractLinkedProcessorFilter<T> filter);
	
}
