package com.edan.rapid.client.core;

import com.edan.rapid.client.RapidInvoker;
import com.edan.rapid.client.RapidProtocol;
import com.edan.rapid.client.RapidService;
import com.edan.rapid.client.support.dubbo.DubboConstants;
import com.edan.rapid.common.config.DubboServiceInvoker;
import com.edan.rapid.common.config.HttpServiceInvoker;
import com.edan.rapid.common.config.ServiceDefinition;
import com.edan.rapid.common.config.ServiceInvoker;
import com.edan.rapid.common.constants.BasicConst;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <B>主类名称：</B>RapidAnnotationScanner<BR>
 * <B>概要说明：</B>注解扫描类, 用于扫描所有的用户定义的 @RapidService 和 @RapidInvoker<BR>
 * @author JiFeng
 * @since 2021年12月18日 上午1:01:31
 */
public class RapidAnnotationScanner {

	private RapidAnnotationScanner() {
	}
	
	private static class SingletonHolder {
		static final RapidAnnotationScanner INSTANCE = new RapidAnnotationScanner();
	}
	
	public static RapidAnnotationScanner getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * <B>方法名称：</B>scanbuilder<BR>
	 * <B>概要说明：</B>扫描传入的Bean对象，最终返回一个ServiceDefinition<BR>
	 * @author JiFeng
	 * @since 2021年12月18日 上午1:06:50
	 * @param bean
	 * @param args 额外的参数选项：注册dubbo时需要使用ServiceBean
	 * @return ServiceDefinition
	 */
	public synchronized ServiceDefinition scanbuilder(Object bean, Object... args) {
		
		Class<?> clazz = bean.getClass();
		boolean isPresent = clazz.isAnnotationPresent(RapidService.class);
		
		if(isPresent) {
			RapidService rapidService = clazz.getAnnotation(RapidService.class);
			String serviceId = rapidService.serviceId();
			RapidProtocol protocol = rapidService.protocol();
			String patternPath = rapidService.patternPath();
			String version = rapidService.version();
			
			ServiceDefinition serviceDefinition = new ServiceDefinition();
			Map<String /* invokerPath */, ServiceInvoker> invokerMap = new HashMap<String, ServiceInvoker>();
			
			Method[] methods = clazz.getMethods();
			if(methods != null && methods.length > 0) {
				for(Method method : methods) {
					RapidInvoker rapidInvoker = method.getAnnotation(RapidInvoker.class);
					if(rapidInvoker == null) {
						continue;
					}
					String path = rapidInvoker.path();
					
					switch (protocol) {
						case HTTP:
							HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path, bean, method);
							invokerMap.put(path, httpServiceInvoker);
							break;
						case DUBBO:
							ServiceBean<?> serviceBean = (ServiceBean<?>)args[0];
							DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
							//	dubbo version reset for serviceDefinition version
							String dubboVersion = dubboServiceInvoker.getVersion();
							if(!StringUtils.isBlank(dubboVersion)) {
								version = dubboVersion;
							}
							invokerMap.put(path, dubboServiceInvoker);
							break;
						default:
							break;
					}
				}
			}
			//	设置属性
			serviceDefinition.setUniqueId(serviceId + BasicConst.COLON_SEPARATOR + version); 
			serviceDefinition.setServiceId(serviceId);
			serviceDefinition.setVersion(version);
			serviceDefinition.setProtocol(protocol.getCode());
			serviceDefinition.setPatternPath(patternPath);
			serviceDefinition.setEnable(true);
			serviceDefinition.setInvokerMap(invokerMap);
			return serviceDefinition;
		}
		
		return null;
	}

	/**
	 * <B>方法名称：</B>createHttpServiceInvoker<BR>
	 * <B>概要说明：</B>构建HttpServiceInvoker对象<BR>
	 * @author JiFeng
	 * @since 2021年12月18日 上午1:19:33
	 * @param path
	 * @param bean
	 * @param method 
	 * @return HttpServiceInvoker
	 */
	private HttpServiceInvoker createHttpServiceInvoker(String path, Object bean, Method method) {
		HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
		httpServiceInvoker.setInvokerPath(path);
		return httpServiceInvoker;
	}
	
	/**
	 * <B>方法名称：</B>createDubboServiceInvoker<BR>
	 * <B>概要说明：</B>构建DubboServiceInvoker对象<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 上午11:37:40
	 * @param path
	 * @param bean
	 * @param method
	 * @return DubboServiceInvoker
	 */
	private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
		DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
		dubboServiceInvoker.setInvokerPath(path);
		
		String methodName = method.getName();
		String registerAddress = serviceBean.getRegistry().getAddress();
		String interfaceClass = serviceBean.getInterface();
		
		dubboServiceInvoker.setRegisterAddress(registerAddress);
		dubboServiceInvoker.setMethodName(methodName);
		dubboServiceInvoker.setInterfaceClass(interfaceClass);
		
		String[] parameterTypes = new String[method.getParameterCount()];
		Class<?>[] classes = method.getParameterTypes();
		for(int i = 0; i < classes.length; i ++) {
			parameterTypes[i] = classes[i].getName();
		}
		dubboServiceInvoker.setParameterTypes(parameterTypes);
		
		Integer seriveTimeout = serviceBean.getTimeout();
		if(seriveTimeout == null || seriveTimeout.intValue() == 0) {
			ProviderConfig providerConfig = serviceBean.getProvider();
			if(providerConfig != null) {
				Integer providerTimeout = providerConfig.getTimeout();
				if(providerTimeout == null || providerTimeout.intValue() == 0) {
					seriveTimeout = DubboConstants.DUBBO_TIMEOUT;
				} else {
					seriveTimeout = providerTimeout;
				}
			}
		}
		dubboServiceInvoker.setTimeout(seriveTimeout);
		
		String dubboVersion = serviceBean.getVersion();
		dubboServiceInvoker.setVersion(dubboVersion);
		
		return dubboServiceInvoker;
	}
	
}
