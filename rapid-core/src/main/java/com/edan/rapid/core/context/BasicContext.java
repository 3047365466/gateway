package com.edan.rapid.core.context;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * <B>主类名称：</B>BasicContext<BR>
 * <B>概要说明：</B>基础上下文实现类<BR>
 * @author JiFeng
 * @since 2021年12月9日 上午11:12:49
 */
@Getter
@Setter
public abstract class BasicContext implements Context {

	protected final String protocol;
	
	protected final ChannelHandlerContext nettyCtx;
	
	protected final boolean keepAlive;
	
	//	上下文的status标识
	protected volatile int status = Context.RUNNING;
	
	//	保存所有的上下文参数集合
	protected final Map<AttributeKey<?>, Object> attributes = new HashMap<AttributeKey<?>, Object>();
	
	//	在请求过程中出现异常则设置异常对象
	protected Throwable throwable;
	
	//	定义是否已经释放请求资源
	protected final AtomicBoolean requestReleased = new AtomicBoolean(false);
	
	//	存放回调函数的集合
	protected List<Consumer<Context>> completedCallbacks;

	protected long SRTime;

	protected long SSTime;

	protected long RSTime;

	protected long RRTime;
	
	public BasicContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
		this.protocol = protocol;
		this.nettyCtx = nettyCtx;
		this.keepAlive = keepAlive;
	}
	
	@Override
	public String getProtocol() {
		return this.protocol;
	}
	
	@Override
	public ChannelHandlerContext getNettyCtx() {
		return this.nettyCtx;
	}
	
	@Override
	public boolean isKeepAlive() {
		return this.keepAlive;
	}
	
	@Override
	public void runned() {
		status = Context.RUNNING;
	}
	
	@Override
	public void writtened(){
		status = Context.WRITTEN;
	}

	@Override
	public void completed(){
		status = Context.COMPLETED;
	}
	
	@Override
	public void terminated(){
		status = Context.TERMINATED;
	}
	
	@Override
	public boolean isRunning(){
		return status == Context.RUNNING;
	}
	
	@Override
	public boolean isWrittened(){
		return status == Context.WRITTEN;
	}
	
	@Override
	public boolean isCompleted(){
		return status == Context.COMPLETED;
	}
	
	@Override
	public boolean isTerminated(){
		return status == Context.TERMINATED;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(AttributeKey<T> key) {
		return (T) attributes.get(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T putAttribute(AttributeKey<T> key, T value) {
		return (T) attributes.put(key, value);
	}
	
	@Override
	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	@Override
	public Throwable getThrowable() {
		return this.throwable;
	}
	
	@Override
	public void releaseRequest() {
		this.requestReleased.compareAndSet(false, true);
	}
	
	@Override
	public void completedCallback(Consumer<Context> consumer) {
		if(completedCallbacks == null) {
			completedCallbacks = new ArrayList<>();
		}
		completedCallbacks.add(consumer);
	}
	
	@Override
	public void invokeCompletedCallback() {
		if(completedCallbacks != null) {
			completedCallbacks.forEach(call -> call.accept(this));
		}
	}
	
}
