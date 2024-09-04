package com.edan.rapid.common.config;

/**
 * <B>主类名称：</B>AbstractServiceInvoker<BR>
 * <B>概要说明：</B>抽象的服务调用接口实现类<BR>
 * @author JiFeng
 * @since 2021年12月11日 上午12:22:00
 */
public class AbstractServiceInvoker implements ServiceInvoker {
	
	protected String invokerPath;
	
	protected String ruleId;
	
	protected int timeout = 5000;

	@Override
	public String getInvokerPath() {
		return invokerPath;
	}

	@Override
	public void setInvokerPath(String invokerPath) {
		this.invokerPath = invokerPath;
	}

	@Override
	public String getRuleId() {
		return ruleId;
	}

	@Override
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;		
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
