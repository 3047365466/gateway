package com.edan.rapid.core.discovery;

import com.alibaba.fastjson.JSONObject;
import com.edan.rapid.common.config.*;
import com.edan.rapid.common.constants.BasicConst;
import com.edan.rapid.common.constants.RapidProtocol;
import com.edan.rapid.common.util.FastJsonConvertUtil;
import com.edan.rapid.common.util.Pair;
import com.edan.rapid.common.util.ServiceLoader;
import com.edan.rapid.core.RapidConfig;
import com.edan.rapid.discovery.api.Notify;
import com.edan.rapid.discovery.api.Registry;
import com.edan.rapid.discovery.api.RegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * <B>主类名称：</B>RegistryManager<BR>
 * <B>概要说明：</B>网关服务的注册中心管理类<BR>
 * @author JiFeng
 * @since 2021年12月19日 下午9:21:39
 */
@Slf4j
public class RegistryManager {

	private RegistryManager() {
	}
	
	private static class SingletonHolder {
		private static final RegistryManager INSTANCE = new RegistryManager();
	}
	
	public static RegistryManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private RapidConfig rapidConfig;
	
	private RegistryService registryService;
	
	@Getter
	private static String superPath;
	
	@Getter
	private static String servicesPath;

	@Getter
	private static String instancesPath;

	@Getter
	private static String rulesPath;
	
	@Getter
	private static String gatewaysPath;
	
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	
	public void initialized(RapidConfig rapidConfig) throws Exception {
		this.rapidConfig = rapidConfig;
		//	1. 路径的设置
		superPath = Registry.PATH + rapidConfig.getNamespace() + BasicConst.BAR_SEPARATOR + rapidConfig.getEnv();
		servicesPath = superPath + Registry.SERVICE_PREFIX;
		instancesPath = superPath + Registry.INSTANCE_PREFIX;
		rulesPath = superPath + Registry.RULE_PREFIX;
		gatewaysPath = superPath + Registry.GATEWAY_PREFIX;
		
		//	2. 初始化加载注册中心对象
		ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
		for(RegistryService registryService : serviceLoader) {
			registryService.initialized(rapidConfig.getRegistryAddress());
			this.registryService = registryService;
		}
		
		//	3. 注册监听
		this.registryService.addWatcherListeners(superPath, new ServiceListener());
		
		//	4.订阅服务
		subscribeService();
		
		//	5.注册自身服务
		RegistryServer registryServer = new RegistryServer(registryService);
		registryServer.registerSelf();
		
	}
	
	/**
	 * <B>方法名称：</B>subscribeService<BR>
	 * <B>概要说明：</B>订阅服务的方法：拉取Etcd注册中心的所有需要使用的元数据信息，解析并放置到缓存中<BR>
	 * 	
	 * 		/edanRapid-dev
	 * 			/services
	 * 				/hello:1.0.0
	 * 				/say:1.0.0
	 * 			/instances
	 * 				/hello:1.0.0/192.168.11.100:1234
	 * 				/hello:1.0.0/192.168.11.101:4321
	 * 					
	 * @author JiFeng
	 * @since 2021年12月19日 下午9:35:00
	 */
	private synchronized void subscribeService() {
		log.info("#RegistryManager#subscribeService  ------------ 	服务订阅开始 	---------------");
		
		try {
			//	1. 加载服务定义和服务实例的集合：获取  servicesPath = /edanRapid-env/services 下面所有的列表
			List<Pair<String, String>> definitionList = this.registryService.getListByPrefixKey(servicesPath);
			
			for(Pair<String, String> definition : definitionList) {
				String definitionPath = definition.getObject1();
				String definitionJson = definition.getObject2();
				
				//	把当前获取的跟目录进行排除
				if(definitionPath.equals(servicesPath)) {
					continue;
				}
				
				//	1.1 加载服务定义集合：
				String uniqueId = definitionPath.substring(servicesPath.length() + 1);
				ServiceDefinition serviceDefinition = parseServiceDefinition(definitionJson);
				DynamicConfigManager.getInstance().putServiceDefinition(uniqueId, serviceDefinition);
				log.info("#RegistryManager#subscribeService 1.1 加载服务定义信息 uniqueId : {}, serviceDefinition : {}", 
						uniqueId,
						FastJsonConvertUtil.convertObjectToJSON(serviceDefinition));
				
				//	1.2 加载服务实例集合：
				//	首先拼接当前服务定义的服务实例前缀路径
				String serviceInstancePrefix = instancesPath + Registry.PATH + uniqueId;
				List<Pair<String, String>> instanceList = this.registryService.getListByPrefixKey(serviceInstancePrefix);
				Set<ServiceInstance> serviceInstanceSet = new HashSet<>();
				for(Pair<String, String> instance : instanceList) {
					String instanceJson = instance.getObject2();
					ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(instanceJson, ServiceInstance.class);
					serviceInstanceSet.add(serviceInstance);
				}
				DynamicConfigManager.getInstance().addServiceInstance(uniqueId, serviceInstanceSet);
				log.info("#RegistryManager#subscribeService 1.2 加载服务实例 uniqueId : {}, serviceDefinition : {}", 			
						uniqueId,
						FastJsonConvertUtil.convertObjectToJSON(serviceInstanceSet));

			}
			
			//	2. 加载规则集合：
			List<Pair<String, String>> ruleList = this.registryService.getListByPrefixKey(rulesPath);
			for(Pair<String, String> r: ruleList) {
				String rulePath = r.getObject1();
				String ruleJson = r.getObject2();
				if(rulePath.endsWith(rulesPath)) {
					continue;
				}
				Rule rule = FastJsonConvertUtil.convertJSONToObject(ruleJson, Rule.class);
				if (Objects.isNull(rule)) {
					log.info("#RegistryManager#subscribeService 2 加载规则信息 反序列化失败");
					continue;
				}
				DynamicConfigManager.getInstance().putRule(rule.getId(), rule);
				log.info("#RegistryManager#subscribeService 2 加载规则信息 ruleId : {}, rule : {}", 			
						rule.getId(),
						FastJsonConvertUtil.convertObjectToJSON(rule));				
			}
			
		} catch (Exception e) {
			log.error("#RegistryManager#subscribeService 服务订阅失败 ", e);
		} finally {
			countDownLatch.countDown();
			log.info("#RegistryManager#subscribeService  ------------ 	服务订阅结束 	---------------");
		}
	}

	/**
	 * <B>方法名称：</B>parseServiceDefinition<BR>
	 * <B>概要说明：</B>把从注册中心拉取过来的json字符串 转换成指定的ServiceDefinition<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午10:02:51
	 * @param definitionJson
	 * @return ServiceDefinition
	 */
	@SuppressWarnings("unchecked")
	private ServiceDefinition parseServiceDefinition(String definitionJson) {
		Map<String, Object> jsonMap = FastJsonConvertUtil.convertJSONToObject(definitionJson, Map.class);
		ServiceDefinition serviceDefinition = new ServiceDefinition();

		//	填充serviceDefinition
		serviceDefinition.setUniqueId((String)jsonMap.get("uniqueId"));
		serviceDefinition.setServiceId((String)jsonMap.get("serviceId"));
		serviceDefinition.setProtocol((String)jsonMap.get("protocol"));
		serviceDefinition.setPatternPath((String)jsonMap.get("patternPath"));
		serviceDefinition.setVersion((String)jsonMap.get("version"));
		serviceDefinition.setEnable((boolean)jsonMap.get("enable"));
		serviceDefinition.setEnvType((String)jsonMap.get("envType"));
		
		Map<String, ServiceInvoker> invokerMap = new HashMap<String, ServiceInvoker>();
		JSONObject jsonInvokerMap = (JSONObject)jsonMap.get("invokerMap");
		
		switch (serviceDefinition.getProtocol()) {
			case RapidProtocol.HTTP:
				Map<String, Object> httpInvokerMap = FastJsonConvertUtil.convertJSONToObject(jsonInvokerMap, Map.class);
				for(Map.Entry<String, Object> me : httpInvokerMap.entrySet()) {
					String path = me.getKey();
					JSONObject jsonInvoker = (JSONObject)me.getValue();
					HttpServiceInvoker httpServiceInvoker = FastJsonConvertUtil.convertJSONToObject(jsonInvoker, HttpServiceInvoker.class);
					invokerMap.put(path, httpServiceInvoker);
				}
				break;
			case RapidProtocol.DUBBO:
				Map<String, Object> dubboInvokerMap = FastJsonConvertUtil.convertJSONToObject(jsonInvokerMap, Map.class);
				for(Map.Entry<String, Object> me : dubboInvokerMap.entrySet()) {
					String path = me.getKey();
					JSONObject jsonInvoker = (JSONObject)me.getValue();
					DubboServiceInvoker dubboServiceInvoker = FastJsonConvertUtil.convertJSONToObject(jsonInvoker, DubboServiceInvoker.class);
					invokerMap.put(path, dubboServiceInvoker);
				}
				break;
			default:
				break;
		}
		
		serviceDefinition.setInvokerMap(invokerMap);
		return serviceDefinition;
	}

	class ServiceListener implements Notify {

		@Override
		public void put(String key, String value) throws Exception {
			countDownLatch.await();
			if(servicesPath.equals(key) ||
					instancesPath.equals(key) ||
					rulesPath.equals(key)) {
				return;
			}
			
			//	如果是服务定义发生变更：
			if(key.contains(servicesPath)) {
				String uniqueId = key.substring(servicesPath.length() + 1);
				//	ServiceDefinition
				ServiceDefinition serviceDefinition = parseServiceDefinition(value);
				DynamicConfigManager.getInstance().putServiceDefinition(uniqueId, serviceDefinition);
				return;
			}
			//	如果是服务实例发生变更：
			if(key.contains(instancesPath)) {
				//	ServiceInstance
				//			hello:1.0.0/192.168.11.100:1234
				String temp = key.substring(instancesPath.length() + 1);
				String[] tempArray = temp.split(Registry.PATH);
				if(tempArray.length == 2) {
					String uniqueId = tempArray[0];
					ServiceInstance serviceInstance = FastJsonConvertUtil.convertJSONToObject(value, ServiceInstance.class);
					DynamicConfigManager.getInstance().updateServiceInstance(uniqueId, serviceInstance);
				}
				return;
			}
			//	如果是规则发生变更：
			if(key.contains(rulesPath)) {
				//	Rule
				String ruleId = key.substring(rulesPath.length() + 1);
				Rule rule = FastJsonConvertUtil.convertJSONToObject(value, Rule.class);
				DynamicConfigManager.getInstance().putRule(ruleId, rule);
				return;
			}
		}

		@Override
		public void delete(String key) throws Exception {
			countDownLatch.await();
			
			if(servicesPath.equals(key) ||
					instancesPath.equals(key) ||
					rulesPath.equals(key)) {
				return;
			}
			
			//	如果是服务定义发生变更：
			if(key.contains(servicesPath)) {
				String uniqueId = key.substring(servicesPath.length() + 1);
				DynamicConfigManager.getInstance().removeServiceDefinition(uniqueId);
				DynamicConfigManager.getInstance().removeServiceInstancesByUniqueId(uniqueId);
				return;
			}
			//	如果是服务实例发生变更：
			if(key.contains(instancesPath)) {
				//	hello:1.0.0/192.168.11.100:1234
				String temp = key.substring(instancesPath.length() + 1);
				String[] tempArray = temp.split(Registry.PATH);
				if(tempArray.length == 2) {
					String uniqueId = tempArray[0];
					String serviceInstanceId = tempArray[1];
					DynamicConfigManager.getInstance().removeServiceInstance(uniqueId, serviceInstanceId);
				}				
				return;
			}
			//	如果是规则发生变更：
			if(key.contains(rulesPath)) {
				String ruleId = key.substring(rulesPath.length() + 1);
				DynamicConfigManager.getInstance().removeRule(ruleId);
				return;
			}
		}
	} 
	
	/**
	 * <B>主类名称：</B>RegistryServer<BR>
	 * <B>概要说明：</B>网关自身注册服务<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午11:21:45
	 */
	class RegistryServer {
		
		private RegistryService registryService;
		
		private String selfPath;
		
		public RegistryServer(RegistryService registryService) throws Exception {
			this.registryService = registryService;
			this.registryService.registerPathIfNotExists(superPath, "", true);
			this.registryService.registerPathIfNotExists(gatewaysPath, "", true);
			this.selfPath = gatewaysPath + Registry.PATH + rapidConfig.getRapidId();
		}
		
		public void registerSelf() throws Exception {
			String rapidConfigJson = FastJsonConvertUtil.convertObjectToJSON(rapidConfig);
			this.registryService.registerPathIfNotExists(selfPath, rapidConfigJson, false);
		}
	}

}
