package com.edan.rapid.core.balance;

import com.edan.rapid.common.config.ServiceInstance;
import com.edan.rapid.common.enums.LoadBalanceStrategy;
import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.common.exception.RapidResponseException;
import com.edan.rapid.core.context.AttributeKey;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.helper.DubboReferenceHelper;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Constants;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <B>主类名称：</B>DubboLoadBalance<BR>
 * <B>概要说明：</B>使用dubbo的SPI扩展点实现<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午8:32:37
 */
public class DubboLoadBalance implements org.apache.dubbo.rpc.cluster.LoadBalance {

	public static final String NAME = "rlb";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
		System.err.println("---------------- DubboLoadBalance into  --------------");

		RapidContext rapidContext = (RapidContext)RpcContext.getContext().get(DubboReferenceHelper.DUBBO_TRANSFER_CONTEXT);
		LoadBalanceStrategy loadBalanceStrategy = rapidContext.getAttribute(AttributeKey.DUBBO_LOADBALANCE_STRATEGY);
		LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceStrategy);
		Set<ServiceInstance> instanceWrappers = new HashSet<>();
		for(Invoker<?> invoker : invokers) {
			instanceWrappers.add(new ServiceInstanceWrapper<>(invoker, invocation));
		}
		// 	把dubbo invokers的服务实例列表 转成自己能够认识的ServiceInstance，设置到全局上下文对象里
		rapidContext.putAttribute(AttributeKey.MATCH_INSTANCES, instanceWrappers);

		ServiceInstance serviceInstance = loadBalance.select(rapidContext);
		if(serviceInstance instanceof ServiceInstanceWrapper) {
			return ((ServiceInstanceWrapper)serviceInstance).getInvoker();
		} else {
			//	永远不会走
			throw new RapidResponseException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
		}
	}

	public static class ServiceInstanceWrapper<T> extends ServiceInstance {

		private static final long serialVersionUID = -6254823227724967507L;

		private final Invoker<T> invoker;

		public ServiceInstanceWrapper(Invoker<T> invoker, Invocation invocation) {
			this.invoker = invoker;
			this.setServiceInstanceId(invoker.getUrl().getAddress());
			this.setAddress(invoker.getUrl().getAddress());
			this.setUniqueId(invoker.getUrl().getServiceKey());
			this.setRegisterTime(invoker.getUrl().getParameter(CommonConstants.TIMESTAMP_KEY, 0L));
			this.setWeight(invoker.getUrl().getMethodParameter(invocation.getMethodName(),
					Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT));
			this.setVersion(invoker.getUrl().getParameter(CommonConstants.VERSION_KEY));
			this.setEnable(true);
		}

		public Invoker<T> getInvoker() {
			return invoker;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(this == null || getClass() != o.getClass()) return false;
			ServiceInstanceWrapper<?> serviceInstanceWrapper = (ServiceInstanceWrapper<?>)o;
			return Objects.equals(this.address, serviceInstanceWrapper.address);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.address);
		}

	}

}
