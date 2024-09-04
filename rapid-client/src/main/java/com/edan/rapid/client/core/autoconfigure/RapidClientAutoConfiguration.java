package com.edan.rapid.client.core.autoconfigure;

import com.edan.rapid.client.support.dubbo.Dubbo27ClientRegisteryManager;
import com.edan.rapid.client.support.springmvc.SpringMVCClientRegisteryManager;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;

/**
 * <B>主类名称：</B>RapidClientAutoConfiguration<BR>
 * <B>概要说明：</B>SpringBoot自动装配加载类<BR>
 * @author JiFeng
 * @since 2021年12月18日 上午1:44:08
 */
@Configuration
@EnableConfigurationProperties(RapidProperties.class)
@ConditionalOnProperty(prefix = RapidProperties.RAPID_PREFIX, name = {"registryAddress", "namespace"})
public class RapidClientAutoConfiguration {

	@Autowired
	private RapidProperties rapidProperties;
	
	@Bean
	@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
	@ConditionalOnMissingBean(SpringMVCClientRegisteryManager.class)
	public SpringMVCClientRegisteryManager springMVCClientRegisteryManager() throws Exception {
		return new SpringMVCClientRegisteryManager(rapidProperties);
	}
	
	@Bean
	@ConditionalOnClass({ServiceBean.class})
	@ConditionalOnMissingBean(Dubbo27ClientRegisteryManager.class)
	public Dubbo27ClientRegisteryManager dubbo27ClientRegisteryManager() throws Exception {
		return new Dubbo27ClientRegisteryManager(rapidProperties);
	}
	
}
