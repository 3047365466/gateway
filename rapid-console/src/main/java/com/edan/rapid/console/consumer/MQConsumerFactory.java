package com.edan.rapid.console.consumer;

import com.edan.rapid.common.constants.BasicConst;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * <B>主类名称：</B>MQConsumerFactory<BR>
 * <B>概要说明：</B>MQConsumerFactory<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午10:50:54
 */
public class MQConsumerFactory {

	private static class SingletonHolder {
		static final MQConsumerFactory INSTANCE = new MQConsumerFactory();
	}
	
	public static MQConsumerFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	private MQConsumerFactory() {
	}
    
	private static ConcurrentHashMap<String /* consumerId = groupId + $ + topic */, MetricConsumer> consumers = new ConcurrentHashMap<String, MetricConsumer>();

	public MetricConsumer createConsumer(String serverAddress, String groupId, String topicNamePrefix, int no){
		String consumerId = groupId + BasicConst.BAR_SEPARATOR + no;
		try {
			Properties props = new Properties();
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);
			props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); 
			props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
			props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
			MetricConsumer consumer = new MetricConsumer(consumerId, props);
			/**
			 * 	采用正则订阅，新增 topic 时，consumer 自动感知，无需重启
			 **/
			consumer.subscribe(Pattern.compile("^" + topicNamePrefix + "[a-zA-Z0-9_]*"));
			consumers.put(consumerId, consumer);
			return consumer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void stopConsumer(String consumerId){
		if(consumers.get(consumerId)!=null){
			consumers.get(consumerId).stop();
			consumers.remove(consumerId);
		}
	}		
	
	public void stopConsumers(){
		for(String consumerId: consumers.keySet()){
			stopConsumer(consumerId);
		}
		consumers.clear();
	}
	
	public static ConcurrentHashMap<String, MetricConsumer> getConsumers(){
		return consumers;
	}
			
}