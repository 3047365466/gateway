package com.edan.rapid.console;

import com.edan.rapid.console.consumer.MQConsumerFactory;
import com.edan.rapid.console.consumer.MetricConsumer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * <B>主类名称：</B>ConsumerContainer<BR>
 * <B>概要说明：</B>ConsumerContainer<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午10:50:27
 */
@Slf4j
public class ConsumerContainer {

	public ConsumerContainer(RapidConsoleProperties rapidConsoleProperties) {
		for(int i = 0; i < rapidConsoleProperties.getConsumerNum(); i ++) {
			MQConsumerFactory.getInstance().createConsumer(rapidConsoleProperties.getKafkaAddress(),
					rapidConsoleProperties.getGroupId(),
					rapidConsoleProperties.getTopicNamePrefix(),
					i);
		}
	}
	
	public void start() {
		runReactorConsumer();
	}
	
	public void stop() {
		MQConsumerFactory.getInstance().stopConsumers();
	}

	private void runReactorConsumer() {
		for(Map.Entry<String, MetricConsumer> me : MQConsumerFactory.getConsumers().entrySet()) {
			MetricConsumer mqConsumer = me.getValue();
			mqConsumer.start();
		}
		log.info("#ConsumerServer# started...");
	}
	
}