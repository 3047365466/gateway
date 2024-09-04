package com.edan.rapid.core.netty.processor.filter;

import com.edan.rapid.core.context.Context;

/**
 * <B>主类名称：</B>DefaultProcessorFilterChain<BR>
 * <B>概要说明：</B>最终的链表实现类<BR>
 * @author JiFeng
 * @since 2021年12月14日 上午1:46:37
 */
public class DefaultProcessorFilterChain extends ProcessorFilterChain<Context> {

	private final String id;
	
	public DefaultProcessorFilterChain(String id) {
		this.id = id;
	}
	
	/**
	 * 	虚拟头结点：dummyHead
	 */
	AbstractLinkedProcessorFilter<Context> first = new AbstractLinkedProcessorFilter<Context>() {
		
		@Override
		public void entry(Context ctx, Object... args) throws Throwable {
			super.fireNext(ctx, args);
		}
		
		@Override
		public boolean check(Context ctx) throws Throwable {
			return true;
		}
	};
	
	/**
	 * 	尾节点
	 */
	AbstractLinkedProcessorFilter<Context> end = first;

	@Override
	public void addFirst(AbstractLinkedProcessorFilter<Context> filter) {
		filter.setNext(first.getNext());
		first.setNext(filter);
		if(end == first) {
			end = filter;
		}
	}

	@Override
	public void addLast(AbstractLinkedProcessorFilter<Context> filter) {
		end.setNext(filter);
		end = filter;
	}
	
	@Override
	public void setNext(AbstractLinkedProcessorFilter<Context> filter) {
		addLast(filter);
	}
	
	@Override
	public AbstractLinkedProcessorFilter<Context> getNext() {
		return first.getNext();
	}
	
	
	@Override
	public boolean check(Context ctx) throws Throwable {
		return true;
	}
	
	@Override
	public void entry(Context ctx, Object... args) throws Throwable {
		first.transformEntry(ctx, args);
	}

	public String getId() {
		return id;
	}

}
