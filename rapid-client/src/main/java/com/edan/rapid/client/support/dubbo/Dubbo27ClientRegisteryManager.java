package com.edan.rapid.client.support.dubbo;

import com.edan.rapid.client.core.AbstractClientRegisteryManager;
import com.edan.rapid.client.core.RapidAnnotationScanner;
import com.edan.rapid.client.core.autoconfigure.RapidProperties;
import com.edan.rapid.common.config.ServiceDefinition;
import com.edan.rapid.common.config.ServiceInstance;
import com.edan.rapid.common.constants.BasicConst;
import com.edan.rapid.common.constants.RapidConst;
import com.edan.rapid.common.util.NetUtils;
import com.edan.rapid.common.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

/**
 * <B>主类名称：</B>Dubbo27ClientRegisteryManager<BR>
 * <B>概要说明：</B>dubbo 2.7.x 客户端注册管理类实现<BR>
 * @author JiFeng
 * @since 2021年12月19日 上午11:18:55
 */
@Slf4j
public class Dubbo27ClientRegisteryManager extends AbstractClientRegisteryManager implements EnvironmentAware, ApplicationListener<ApplicationEvent> {

	public Dubbo27ClientRegisteryManager(RapidProperties rapidProperties) throws Exception {
		super(rapidProperties);
	}
	
	private Environment environment;
	
	private static final Set<Object> uniqueBeanSet = new HashSet<>();

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	@PostConstruct
	private void init() {
		String port = environment.getProperty(DubboConstants.DUBBO_PROTOCOL_PORT);
		if(StringUtils.isEmpty(port)) {
			log.error("Rapid Dubbo服务未启动");
			return;
		}
		whetherStart = true;
	}
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(!whetherStart) {
			return;
		}
		if(event instanceof ServiceBeanExportedEvent) {
			ServiceBean<?> serviceBean = ((ServiceBeanExportedEvent)event).getServiceBean();
			try {
				registeryServiceBean(serviceBean);
			} catch (Exception e) {
				log.error("Rapid Dubbo 注册服务ServiceBean 失败，ServiceBean = {}", serviceBean, e);
			}
		} else if(event instanceof ApplicationStartedEvent){
			//	START:::
			System.err.println("******************************************");
			System.err.println("**        Rapid Dubbo Started           **");
			System.err.println("******************************************");
		}
	}

	/**
	 * <B>方法名称：</B>registeryServiceBean<BR>
	 * <B>概要说明：</B>注册Dubbo服务：从ServiceBeanExportedEvent获取ServiceBean对象<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 上午11:27:08
	 * @param serviceBean
	 * @throws Exception 
	 */
	private void registeryServiceBean(ServiceBean<?> serviceBean) throws Exception {
		
		Object bean = serviceBean.getRef();
		if(uniqueBeanSet.add(bean)) {
			ServiceDefinition serviceDefinition = RapidAnnotationScanner.getInstance().scanbuilder(bean, serviceBean);
			if(serviceDefinition != null) {
				//	设置环境
				serviceDefinition.setEnvType(getEnv());
				//	注册服务定义
				registerServiceDefinition(serviceDefinition);
				
				//	注册服务实例：
				ServiceInstance serviceInstance = new ServiceInstance();
				String localIp = NetUtils.getLocalIp();
				int port = serviceBean.getProtocol().getPort();
				String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
				String address = serviceInstanceId;
				String uniqueId = serviceDefinition.getUniqueId();
				String version = serviceDefinition.getVersion();
				
				serviceInstance.setServiceInstanceId(serviceInstanceId);
				serviceInstance.setUniqueId(uniqueId);
				serviceInstance.setAddress(address);
				serviceInstance.setWeight(RapidConst.DEFAULT_WEIGHT);
				serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
				serviceInstance.setVersion(version);
				
				registerServiceInstance(serviceInstance);
			}
		}
	}
	
}
