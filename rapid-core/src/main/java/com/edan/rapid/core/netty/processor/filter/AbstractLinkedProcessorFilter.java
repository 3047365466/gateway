package com.edan.rapid.core.netty.processor.filter;

import com.edan.rapid.core.context.Context;

/**
 * <B>主类名称：</B>AbstractLinkedProcessorFilter<BR>
 * <B>概要说明：</B>抽象的带有链表形式的过滤器<BR>
 * @author JiFeng
 * @since 2021年12月14日 上午1:03:25
 */
public abstract class AbstractLinkedProcessorFilter<T> implements ProcessorFilter<Context> {

	//	做一个链表里面的一个元素，必须要有下一个元素的引用
	protected AbstractLinkedProcessorFilter<T> next = null;
	
	@Override
	public void fireNext(Context ctx, Object... args) throws Throwable {
		
		if(next != null) {
			if(!next.check(ctx)) {
				next.fireNext(ctx, args);
			} else {
				next.transformEntry(ctx, args);
			}
		} else {
			//	没有下一个节点了，已经到了链表的最后一个节点
			return;
		}
		
	}
	
	@Override
	public void transformEntry(Context ctx, Object... args) throws Throwable {
		//	子类调用：这里就是真正执行下一个节点(元素)的操作
		entry(ctx, args);
	}
	
	public void setNext(AbstractLinkedProcessorFilter<T> next) {
		this.next = next;
	}
	
	public AbstractLinkedProcessorFilter<T> getNext() {
		return next;
	}
	
}
