package com.edan.rapid.common.config;

/**
 * <B>主类名称：</B>ServiceInvoker<BR>
 * <B>概要说明：</B>服务调用的接口模型描述<BR>
 * @author JiFeng
 * @since 2021年12月11日 上午12:16:31
 */
public interface ServiceInvoker {

	/**
	 * <B>方法名称：</B>getInvokerPath<BR>
	 * <B>概要说明：</B>获取真正的服务调用的全路径<BR>
	 * @author JiFeng
	 * @since 2021年12月11日 上午12:17:01
	 * @return invokerPath
	 */
	String getInvokerPath();
	
	void setInvokerPath(String invokerPath);
	
	/**
	 * <B>方法名称：</B>getRuleId<BR>
	 * <B>概要说明：</B>获取指定服务调用绑定的唯一规则<BR>
	 * @author JiFeng
	 * @since 2021年12月11日 上午12:17:50
	 * @return
	 */
	String getRuleId();
	
	void setRuleId(String ruleId);
	
	/**
	 * <B>方法名称：</B>getTimeout<BR>
	 * <B>概要说明：</B>获取该服务调用(方法)的超时时间<BR>
	 * @author JiFeng
	 * @since 2021年12月11日 上午12:20:12
	 * @return
	 */
	int getTimeout();
	
	void setTimeout(int timeout);
	
}
