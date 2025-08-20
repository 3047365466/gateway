package com.edan.rapid.core.balance;

import com.edan.rapid.common.config.ServiceInstance;
import com.edan.rapid.common.util.TimeUtil;
import com.edan.rapid.core.context.AttributeKey;
import com.edan.rapid.core.context.RapidContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <B>主类名称：</B>AbstractLoadBalance<BR>
 * <B>概要说明：</B>抽象负载均衡类：主要实现预热的功能<BR>
 * @author JiFeng
 * @since 2021年12月20日 上午12:01:21
 */
public abstract class AbstractLoadBalance implements LoadBalance {

	@Override
	public ServiceInstance select(RapidContext context) {
		
		//	MATCH_INSTANCES：服务实例列表现在还没有填充，需要LoadBalancePreFilter的时候进行获取并设置
		Set<ServiceInstance> matchInstance = context.getAttribute(AttributeKey.MATCH_INSTANCES);
		if(matchInstance == null || matchInstance.size() == 0) {
			return null;
		}
		
		List<ServiceInstance> instances = new ArrayList<ServiceInstance>(matchInstance);
		if(instances.size() == 1) {
			return instances.get(0);
		}
		
		ServiceInstance instance = doSelect(context, instances);
		context.putAttribute(AttributeKey.LOAD_INSTANCE, instance);
		return instance;
	}

	/**
	 * <B>方法名称：</B>doSelect<BR>
	 * <B>概要说明：</B>子类实现指定的负载均衡策略选择一个服务<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 上午12:10:45
	 * @param context
	 * @param instances
	 * @return
	 */
	protected abstract ServiceInstance doSelect(RapidContext context, List<ServiceInstance> instances);

	
	protected static int getWeight(ServiceInstance instance) {
		int weight = instance.getWeight() == null ? LoadBalance.DEFAULT_WEIGHT : instance.getWeight();
		if(weight > 0) {
			//	服务启动注册的时间
			long timestamp = instance.getRegisterTime();
			if(timestamp > 0L) {
				//	服务启动了多久：当前时间 - 注册时间
				int upTime = (int)(TimeUtil.currentTimeMillis() - timestamp);
				//	默认预热时间 5min
				int warmup = LoadBalance.DEFAULT_WARMUP;
				if(upTime > 0 && upTime < warmup) {
					weight = calculateWramUpWeight(upTime, warmup, weight);
				}
			}
		}
		return weight;
	}

	/**
	 * <B>方法名称：</B>calculateWramUpWeight<BR>
	 * <B>概要说明：</B>计算服务在预热时间内的新权重<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 上午12:16:18
	 * @param upTime
	 * @param warmup
	 * @param weight
	 * @return
	 */
	private static int calculateWramUpWeight(int upTime, int warmup, int weight) {
		int ww =(int)((float)upTime / ((float)warmup / (float) weight));
		return ww < 1 ? 1 : (ww > weight ? weight : ww);
	}
	
}
