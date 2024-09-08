package com.edan.rapid.common.config;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <B>主类名称：</B>DynamicConfigManager<BR>
 * <B>概要说明：</B>动态服务缓存配置管理类<BR>
 * @author JiFeng
 * @since 2021年12月11日 上午1:00:56
 */
public class DynamicConfigManager {
	
	//	服务的定义集合：uniqueId代表服务的唯一标识
	private ConcurrentHashMap<String /* uniqueId */ , ServiceDefinition>  serviceDefinitionMap = new ConcurrentHashMap<>();
	
	//	服务的实例集合：uniqueId与一对服务实例对应
	private ConcurrentHashMap<String /* uniqueId */ , Set<ServiceInstance>>  serviceInstanceMap = new ConcurrentHashMap<>();

	//	规则集合
	private ConcurrentHashMap<String /* ruleId */ , Rule>  ruleMap = new ConcurrentHashMap<>();
	
	private DynamicConfigManager() {
	}

	public Set<ServiceInstance> getServiceInstanceByUniqueId(String uniqueId) {
		return serviceInstanceMap.get(uniqueId);
	}

	private static class SingletonHolder {
		private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
	}
	
	
	/***************** 	对服务定义缓存进行操作的系列方法 	***************/
	
	public static DynamicConfigManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void putServiceDefinition(String uniqueId, ServiceDefinition serviceDefinition) {
		serviceDefinitionMap.put(uniqueId, serviceDefinition);;
	}
	
	public ServiceDefinition getServiceDefinition(String uniqueId) {
		return serviceDefinitionMap.get(uniqueId);
	}
	
	public void removeServiceDefinition(String uniqueId) {
		serviceDefinitionMap.remove(uniqueId);
	}
	
	public ConcurrentHashMap<String, ServiceDefinition> getServiceDefinitionMap() {
		return serviceDefinitionMap;
	}
	
	/***************** 	对服务实例缓存进行操作的系列方法 	***************/

	public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		if (Objects.nonNull(set)) {
			set.add(serviceInstance);
		}
	}

	public void addServiceInstance(String uniqueId, Set<ServiceInstance> serviceInstanceSet) {
		serviceInstanceMap.put(uniqueId, serviceInstanceSet);
	}
	
	public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		if (Objects.nonNull(set)) {
			Iterator<ServiceInstance> it = set.iterator();
			while(it.hasNext()) {
				ServiceInstance is = it.next();
				if(is.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
					it.remove();
					break;
				}
			}
			set.add(serviceInstance);
		}
	}
	
	public void removeServiceInstance(String uniqueId, String serviceInstanceId) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		// 唯一标识不存在
		if (Objects.nonNull(set)) {
			Iterator<ServiceInstance> it = set.iterator();
			while(it.hasNext()) {
				ServiceInstance is = it.next();
				if(is.getServiceInstanceId().equals(serviceInstanceId)) {
					it.remove();
					break;
				}
			}
		}
	}
	
	public void removeServiceInstancesByUniqueId(String uniqueId) {
		serviceInstanceMap.remove(uniqueId);
	}
	
		
	/***************** 	对规则缓存进行操作的系列方法 	***************/
	
	public void putRule(String ruleId, Rule rule) {
		ruleMap.put(ruleId, rule);
	}
	
	public Rule getRule(String ruleId) {
		return ruleMap.get(ruleId);
	}
	
	public void removeRule(String ruleId) {
		ruleMap.remove(ruleId);
	}
	
	public ConcurrentHashMap<String, Rule> getRuleMap() {
		return ruleMap;
	}
	

}
