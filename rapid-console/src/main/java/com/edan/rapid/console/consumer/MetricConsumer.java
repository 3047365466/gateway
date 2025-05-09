package com.edan.rapid.console.consumer;

import com.edan.rapid.common.concurrent.thread.NamedThreadFactory;
import com.edan.rapid.common.metric.Metric;
import com.edan.rapid.common.metric.MetricType;
import com.edan.rapid.common.util.FastJsonConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <B>主类名称：</B>MetricConsumer<BR>
 * <B>概要说明：</B>MetricConsumer<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午10:50:44
 */
@Slf4j
public class MetricConsumer extends KafkaConsumer<String, String> {

	private final String consumerId;
	
	private final ExecutorService bossExecutor;
	
	private final ExecutorService workExecutor;
	
	private volatile boolean isRunning;

	public MetricConsumer(String consumerId, Properties properties) {
		super(properties);
		this.consumerId = consumerId;
		this.bossExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("bossExecutor-" + consumerId, true));
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		this.workExecutor = new ThreadPoolExecutor(availableProcessors,
				availableProcessors,
				60L,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(65535),
				new NamedThreadFactory("workExecutor-" + consumerId, false),
				new ThreadPoolExecutor.DiscardPolicy());  // 拒绝策略, 不执行任何操作
	}

	public void start() {
		this.isRunning = true;
		this.bossExecutor.submit(() -> {
			while (isRunning) {
				ConsumerRecords<String, String> records = poll(Duration.of(1L, ChronoUnit.SECONDS));
				for (TopicPartition partition : records.partitions()) {
					List<ConsumerRecord<String, String>> records2partition = records.records(partition);
					for (ConsumerRecord<String, String> record : records2partition) {
						consumeMessage(record);
					}
				}
			}
		});
	}

	/**
	 * <B>方法名称：</B>consumeMessage<BR>
	 * <B>概要说明：</B>多线程消费<BR>
	 * @author hezhuo.Bai-JiFeng
	 * @since 2021年12月1日 下午5:46:59
	 * @param consumerRecord
	 */
	private void consumeMessage(final ConsumerRecord<String, String> consumerRecord) {
		this.workExecutor.execute(() -> {
			try {
				String record = consumerRecord.value();
				Metric metric = FastJsonConvertUtil.convertJSONToObject(record, Metric.class);
				String metricName = metric.getName();
				Number metricValue = metric.getValue().longValue();
				long timestamp = metric.getTimestamp();
				Map<String, String> tags = metric.getTags();
				String type = tags.get(com.edan.rapid.common.metric.MetricType.KEY);
				switch (type) {
					case MetricType.STATISTICS:
						metricStatistics(metricName, metricValue, timestamp, tags);
						break;
					case MetricType.LOAD:
						metricLoad(metricName, metricValue, timestamp, tags);
						break;	
					default:
						break;
				}
				
			} catch (Exception ex) {
				log.error("#AlertMetricConumer# consumeMessage failed! ", ex);
			}
		});
	}
	
	/**
	 * <B>方法名称：</B>metricStatistics<BR>
	 * <B>概要说明：</B>真实消费消息，统计<BR>
	 * @author hezhuo.Bai-JiFeng
	 * @since 2021年12月1日 下午5:46:16
	 * @param metricName
	 * @param value
	 * @param timestamp
	 * @param tags
	 * @throws Exception
	 */
	private void metricStatistics(String metricName,
			Number metricValue,
			long timestamp, Map<String, String> tags) throws Exception {
		System.err.println("metricName: " + metricName 
				+ ", metricValue: "
				+ metricValue
				+ ", timestamp: "
				+ timestamp
				+ ", tags: "
				+ FastJsonConvertUtil.convertObjectToJSON(tags)
				);
	}
	
	/**
	 * <B>方法名称：</B>metricLoad<BR>
	 * <B>概要说明：</B>真实消费消息，负载<BR>
	 * @author hezhuo.Bai-JiFeng
	 * @since 2021年12月1日 下午8:27:01
	 * @param metricName
	 * @param metricValue
	 * @param timestamp
	 * @param tags
	 * @throws Exception
	 */
	private void metricLoad(String metricName,
			Number metricValue,
			long timestamp, Map<String, String> tags) throws Exception {
		System.err.println("metricName: " + metricName 
				+ ", metricValue: "
				+ metricValue
				+ ", timestamp: "
				+ timestamp
				+ ", tags: "
				+ FastJsonConvertUtil.convertObjectToJSON(tags)
				);
	}
	
	public String getConsumerId() {
		return consumerId;
	}

	public void stop() {
		this.isRunning = false;
		this.bossExecutor.shutdown();
		this.workExecutor.shutdown();
	}
}
