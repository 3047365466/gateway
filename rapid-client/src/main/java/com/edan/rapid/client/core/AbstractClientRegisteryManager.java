package com.edan.rapid.client.core;

import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import com.edan.rapid.client.core.autoconfigure.RapidProperties;
import com.edan.rapid.common.config.ServiceDefinition;
import com.edan.rapid.common.config.ServiceInstance;
import com.edan.rapid.common.constants.BasicConst;
import com.edan.rapid.common.util.FastJsonConvertUtil;
import com.edan.rapid.common.util.ServiceLoader;
import com.edan.rapid.discovery.api.Registry;
import com.edan.rapid.discovery.api.RegistryService;

import lombok.extern.slf4j.Slf4j;

/**
 * <B>主类名称：</B>AbstractClientRegisteryManager<BR>
 * <B>概要说明：</B>抽象注册管理器<BR>
 * @author JiFeng
 * @since 2021年12月19日 上午9:40:23
 */
@Slf4j
public abstract class AbstractClientRegisteryManager {

	public static final String PROPERTIES_PATH = "rapid.properties";
	
	public static final String REGISTERYADDRESS_KEY = "registryAddress";
	
	public static final String NAMESPACE_KEY = "namespace";
	
	public static final String ENV_KEY = "env";
	
	protected volatile boolean whetherStart = false;
	
	public static Properties properties = new Properties();
	
	@Getter
	protected static String registryAddress ;
	
	@Getter
	protected static String namespace ;
	
	@Getter
	protected static String env ;
	
	@Getter
	protected static String superPath;
	
	@Getter
	protected static String servicesPath;

	@Getter
	protected static String instancesPath;

	@Getter
	protected static String rulesPath;
	
	private RegistryService registryService;
	
	//	静态代码块读取rapid.properties配置文件
	static {
		// InputStream is = null;
		// is = AbstractClientRegisteryManager.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH);
		// 通过语法糖的形式读取配置文件， 使其自动关闭流
		try (InputStream is = AbstractClientRegisteryManager.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
			if(is != null) {
				properties.load(is);
				registryAddress = properties.getProperty(REGISTERYADDRESS_KEY);
				namespace = properties.getProperty(NAMESPACE_KEY);
				env = properties.getProperty(ENV_KEY);
				if(StringUtils.isBlank(registryAddress)) {
					String errorMessage = "Rapid网关注册配置地址不能为空";
					log.error(errorMessage);
					throw new RuntimeException(errorMessage);
				}
				if(StringUtils.isBlank(namespace)) {
					namespace = RapidProperties.RAPID_PREFIX;
				}
			}
		} catch (Exception e) {
			log.error("#AbstractClientRegisteryManager# InputStream load is error", e);
		}
	}
	
	/**
	 * <B>构造方法</B>AbstractClientRegisteryManager<BR>
	 * 	application.properties/yml 优先级是最高的
	 * @param rapidProperties
	 * @throws Exception 
	 */
	protected AbstractClientRegisteryManager(RapidProperties rapidProperties) throws Exception {
		//	1. 初始化加载配置信息
		if(rapidProperties.getRegistryAddress() != null) {
			registryAddress = rapidProperties.getRegistryAddress();
			namespace = rapidProperties.getNamespace();
			if(StringUtils.isBlank(namespace)) {
				namespace = RapidProperties.RAPID_PREFIX;
			}
			env = rapidProperties.getEnv();
		}
		
		//	2. 初始化加载注册中心对象
		ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
		for(RegistryService registryService : serviceLoader) {
			registryService.initialized(rapidProperties.getRegistryAddress());
			this.registryService = registryService;
		}
		
		//	3. 注册构建顶级目录结构
		generatorStructPath(Registry.PATH + namespace + BasicConst.BAR_SEPARATOR + env);
	}
	
	/**
	 * <B>方法名称：</B>generatorStructPath<BR>
	 * <B>概要说明：</B>注册顶级结构目录路径，只需要构建一次即可<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 上午10:04:32
	 * @param path
	 * @throws Exception
	 */
	private void generatorStructPath(String path) throws Exception {
		superPath = path;
		registryService.registerPathIfNotExists(superPath, "", true);
		registryService.registerPathIfNotExists(servicesPath = superPath + Registry.SERVICE_PREFIX, "", true);
		registryService.registerPathIfNotExists(instancesPath = superPath + Registry.INSTANCE_PREFIX, "", true);
		registryService.registerPathIfNotExists(rulesPath = superPath + Registry.RULE_PREFIX, "", true);
	}

	/**
	 * <B>方法名称：</B>registerServiceDefinition<BR>
	 * <B>概要说明：</B>注册服务定义 对象<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 上午10:08:22
	 * @param serviceDefinition ServiceDefinition
	 * @throws Exception
	 */
	protected void registerServiceDefinition(ServiceDefinition serviceDefinition) throws Exception {
		/**
		 * 	/rapid-env
		 * 		/services
		 * 			/serviceA:1.0.0  ==> ServiceDefinition
		 * 			/serviceA:2.0.0
		 * 			/serviceB:1.0.0
		 * 		/instances
		 * 			/serviceA:1.0.0/192.168.11.100:port	 ==> ServiceInstance
		 * 			/serviceA:1.0.0/192.168.11.101:port
		 * 			/serviceB:1.0.0/192.168.11.102:port
		 * 			/serviceA:2.0.0/192.168.11.103:port
		 * 		/rules
		 * 			/ruleId1	==>	Rule
		 * 			/ruleId2
		 * 		/gateway
		 */
		String key = servicesPath 
				+ Registry.PATH
				+ serviceDefinition.getUniqueId();
		
		if(!registryService.isExistKey(key)) {
			String value = FastJsonConvertUtil.convertObjectToJSON(serviceDefinition);
			registryService.registerPathIfNotExists(key, value, true);
		}
	}
	
	/**
	 * <B>方法名称：</B>registerServiceInstance<BR>
	 * <B>概要说明：</B>注册服务实例方法<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 上午10:10:15
	 * @param serviceInstance ServiceInstance
	 * @throws Exception
	 */
	protected void registerServiceInstance(ServiceInstance serviceInstance) throws Exception {
		String key = instancesPath
				+ Registry.PATH
				+ serviceInstance.getUniqueId()
				+ Registry.PATH
				+ serviceInstance.getServiceInstanceId();
		if(!registryService.isExistKey(key)) {
			String value = FastJsonConvertUtil.convertObjectToJSON(serviceInstance);
			registryService.registerPathIfNotExists(key, value, false);
		}
	}

}
