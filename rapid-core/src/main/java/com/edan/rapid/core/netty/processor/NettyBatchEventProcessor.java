package com.edan.rapid.core.netty.processor;

import com.edan.rapid.common.concurrent.queue.flusher.ParallelFlusher;
import com.edan.rapid.core.RapidConfig;
import com.edan.rapid.core.context.HttpRequestWrapper;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * <B>主类名称：</B>NettyBatchEventProcessor<BR>
 * <B>概要说明：</B>flusher缓冲队列的核心实现, 最终调用的方法还是要回归到NettyCoreProcessor<BR>
 * @author edan
 * @since 2021年12月5日 下午10:11:16
 */
public class NettyBatchEventProcessor implements NettyProcessor {

	private static final String THREAD_NAME_PREFIX = "rapid-flusher-";

	private RapidConfig rapidConfig;
	
	private NettyCoreProcessor nettyCoreProcessor;
	private ParallelFlusher<HttpRequestWrapper> parallelFlusher;
	
	public NettyBatchEventProcessor(RapidConfig rapidConfig, NettyCoreProcessor nettyCoreProcessor) {
		this.rapidConfig = rapidConfig;
		this.nettyCoreProcessor = nettyCoreProcessor;

		ParallelFlusher.Builder<HttpRequestWrapper> builder = new ParallelFlusher.Builder<HttpRequestWrapper>()
				.setBufferSize(rapidConfig.getBufferSize())
				.setProducerType(ProducerType.MULTI)
				.setThreads(rapidConfig.getProcessThread())
				.setWaitStrategy(rapidConfig.getTrueWaitStrategy())
				.setNamePrefix(THREAD_NAME_PREFIX);

		builder.setEventListener(new BatchEventProcessorListener());
		this.parallelFlusher = builder.build();


	}

	@Override
	public void process(HttpRequestWrapper httpRequestWrapper) {
		parallelFlusher.add(httpRequestWrapper);
	}

	@Override
	public void start() {
		parallelFlusher.start();
	}

	@Override
	public void shutdown() {
		parallelFlusher.shutdown();

		
	}
	public class BatchEventProcessorListener implements ParallelFlusher.EventListener<HttpRequestWrapper> {

		@Override
		public void onEvent(HttpRequestWrapper event) throws Exception {
			nettyCoreProcessor.process(event);
		}

		@Override
		public void onException(Throwable ex, long sequence, HttpRequestWrapper event) {

		}
	}
	
}
