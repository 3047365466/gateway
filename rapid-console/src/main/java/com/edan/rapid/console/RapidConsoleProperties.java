package com.edan.rapid.console;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = RapidConsoleProperties.RAPID_CONSOLE_PREFIX)
public class RapidConsoleProperties {

	public static final String RAPID_CONSOLE_PREFIX = "rapid.console";
	
	private String registryAddress;

	private String namespace;
	
	private String kafkaAddress;
	
	private String groupId;
	
	private String topicNamePrefix;
	
	private int ConsumerNum = 1;

	public String getRegistryAddress() {
		return registryAddress;
	}

	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getKafkaAddress() {
		return kafkaAddress;
	}

	public void setKafkaAddress(String kafkaAddress) {
		this.kafkaAddress = kafkaAddress;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getTopicNamePrefix() {
		return topicNamePrefix;
	}

	public void setTopicNamePrefix(String topicNamePrefix) {
		this.topicNamePrefix = topicNamePrefix;
	}

	public int getConsumerNum() {
		return ConsumerNum;
	}

	public void setConsumerNum(int consumerNum) {
		ConsumerNum = consumerNum;
	}
	
}
