package com.edan.rapid.core.balance;

import com.edan.rapid.common.config.ServiceInstance;
import com.edan.rapid.core.context.RapidContext;

/**
 * <B>主类名称：</B>LoadBalance<BR>
 * <B>概要说明：</B>负载均衡最上层的接口定义<BR>
 * @author JiFeng
 * @since 2021年12月19日 下午11:57:59
 */
public interface LoadBalance {
	
	int DEFAULT_WEIGHT = 100;
	
	int DEFAULT_WARMUP = 5 * 60 * 1000;
	
	/**
	 * <B>方法名称：</B>select<BR>
	 * <B>概要说明：</B>从所有实例列表中选择一个实例<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 上午12:02:09
	 * @param context
	 * @return ServiceInstance
	 */
	ServiceInstance select(RapidContext context);

}
