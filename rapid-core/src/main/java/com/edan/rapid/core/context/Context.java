package com.edan.rapid.core.context;

import com.edan.rapid.common.config.Rule;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * <B>主类名称：</B>Context<BR>
 * <B>概要说明：</B>网关上下文接口定义<BR>
 * @author JiFeng
 * @since 2021年12月9日 上午2:01:24
 */
public interface Context {

	//	一个请求正在执行过程中
	int RUNNING = -1;
	
	// 	写回响应标记, 标记当前Context/请求需要写回
	int WRITTEN = 0;
	
	//	当写回成功后, 设置该标记：ctx.writeAndFlush(response);
	int COMPLETED = 1;
	
	//	表示整个网关请求完毕, 彻底结束
	int TERMINATED = 2;
	
	/*************** -- 设置网关的状态系 -- ********************/
	
	/**
	 * <B>方法名称：</B>runned<BR>
	 * <B>概要说明：</B>设置上下文状态为正常运行状态<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:11:34
	 */
	void runned();
	
	/**
	 * <B>方法名称：</B>writtened<BR>
	 * <B>概要说明：</B>设置上下文状态为标记写回<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:12:07
	 */
	void writtened();
	
	/**
	 * <B>方法名称：</B>completed<BR>
	 * <B>概要说明：</B>设置上下文状态为写回结束<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:12:37
	 */
	void completed();
	
	/**
	 * <B>方法名称：</B>terminated<BR>
	 * <B>概要说明：</B>设置上下文状态为最终结束<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:13:09
	 */
	void terminated();
	
	/*************** -- 判断网关的状态系 -- ********************/
	
	boolean isRunning();
	
	boolean isWrittened();
	
	boolean isCompleted();
	
	boolean isTerminated();
	
	/**
	 * <B>方法名称：</B>getProtocol<BR>
	 * <B>概要说明：</B>获取请求转换协议<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:16:07
	 * @return
	 */
	String getProtocol();
	
	/**
	 * <B>方法名称：</B>getRule<BR>
	 * <B>概要说明：</B>获取规则<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:17:07
	 * @return Rule
	 */
	Rule getRule();
	
	/**
	 * <B>方法名称：</B>getRequest<BR>
	 * <B>概要说明：</B>获取请求对象<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:17:50
	 * @return
	 */
	Object getRequest();
	
	/**
	 * <B>方法名称：</B>getResponse<BR>
	 * <B>概要说明：</B>获取响应对象<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:18:13
	 * @return
	 */
	Object getResponse();
	
	/**
	 * <B>方法名称：</B>setResponse<BR>
	 * <B>概要说明：</B>设置响应对象<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:19:31
	 * @param response
	 */
	void setResponse(Object response);

	/**
	 * <B>方法名称：</B>setThrowable<BR>
	 * <B>概要说明：</B>设置异常信息<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:20:58
	 * @param throwable
	 */
	void setThrowable(Throwable throwable);
	
	/**
	 * <B>方法名称：</B>getThrowable<BR>
	 * <B>概要说明：</B>获取异常<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:21:20
	 * @return Throwable
	 */
	Throwable getThrowable();
	
	/**
	 * <B>方法名称：</B>getAttribute<BR>
	 * <B>概要说明：</B>获取上下文参数<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:22:45
	 * @param <T>
	 * @param key 对应的key
	 * @return T
	 */
	<T> T getAttribute(AttributeKey<T> key);
	
	/**
	 * <B>方法名称：</B>putAttribute<BR>
	 * <B>概要说明：</B>保存上下文属性信息<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:24:26
	 * @param <T>
	 * @param key   关键key
	 * @param value 上下文参数值
	 * @return T
	 */
	<T> T putAttribute(AttributeKey<T> key, T value);
	
	/**
	 * <B>方法名称：</B>getNettyCtx<BR>
	 * <B>概要说明：</B>获取Netty的上下文对象<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:25:37
	 * @return ChannelHandlerContext
	 */
	ChannelHandlerContext getNettyCtx();
	
	/**
	 * <B>方法名称：</B>isKeepAlive<BR>
	 * <B>概要说明：</B>是否保持连接<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:26:09
	 * @return
	 */
	boolean isKeepAlive();
	
	/**
	 * <B>方法名称：</B>releaseRequest<BR>
	 * <B>概要说明：</B>释放请求资源的方法<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:27:43
	 */
	void releaseRequest();
	
	/**
	 * <B>方法名称：</B>completedCallback<BR>
	 * <B>概要说明：</B>写回接收回调函数设置<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:30:02
	 * @param consumer
	 */
	void completedCallback(Consumer<Context> consumer);
	
	/**
	 * <B>方法名称：</B>invokeCompletedCallback<BR>
	 * <B>概要说明：</B>回调函数执行<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午2:30:41
	 */
	void invokeCompletedCallback();


	/**
	 * 	SR(Server[Rapid-Core] Received):	网关服务器接收到网络请求
	 * 	SS(Server[Rapid-Core] Send):		网关服务器写回请求
	 * 	RS(Route Send):						网关客户端发送请求
	 * 	RR(Route Received): 				网关客户端收到请求
	 */

	long getSRTime();

	void setSRTime(long sRTime);

	long getSSTime();

	void setSSTime(long sSTime);

	long getRSTime();

	void setRSTime(long rSTime);

	long getRRTime();

	void setRRTime(long rRTime);
	
	
	
	
}
