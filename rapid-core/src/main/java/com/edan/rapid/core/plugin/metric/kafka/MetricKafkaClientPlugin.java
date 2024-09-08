package com.edan.rapid.core.plugin.metric.kafka;

import com.edan.rapid.common.metric.TimeSeries;
import com.edan.rapid.core.RapidConfigLoader;
import com.edan.rapid.core.plugin.Plugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <B>主类名称：</B>MetricKafkaClientPlugin<BR>
 * <B>概要说明：</B>MetricKafkaClientPlugin<BR>
 * @author JiFeng
 * @since 2021年12月21日 上午1:45:26
 */
@Slf4j
public final class MetricKafkaClientPlugin implements Plugin {

    private MetricKafkaClientCollector metricKafkaClientCollector;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private String address;

    public MetricKafkaClientPlugin() {
    }
    
    @Override
    public boolean check() {
    	this.address = RapidConfigLoader.getRapidConfig().getKafkaAddress();
    	if(!StringUtils.isBlank(this.address)) {
    		return true;
    	}
    	return false;
    }
    
    private boolean checkInit() {
    	return this.initialized.get() && this.metricKafkaClientCollector != null;
    }
    
	@Override
	public void init() {
		if(check()) {
			//	//	初始化kafka
			this.metricKafkaClientCollector = new MetricKafkaClientCollector(this.address);
			this.metricKafkaClientCollector.start();	
			this.initialized.compareAndSet(false, true);
		}
	}
	
	@Override
	public void destroy() {
		if(checkInit()) {
			this.metricKafkaClientCollector.shutdown();	
			this.initialized.compareAndSet(true, false);
		}
	}

    public <T extends TimeSeries> void send(T metric) {
        try {
        	if(checkInit()) {
                metricKafkaClientCollector.sendAsync(metric.getDestination(), metric,
                        (metadata, exception) -> {
                            if (exception != null) {
                                log.error("#MetricKafkaClientSender# callback exception, metric: {}, {}", metric.toString(), exception.getMessage());
                            }
                        }
                );       		
        	}
        } catch (Exception e) {
            log.error("#MetricKafkaClientSender# send exception, metric: {}", metric.toString(), e);
        }
    }

    public <T extends TimeSeries> void sendBatch(List<T> metricList) {
        for (T metric : metricList) {
            send(metric);
        }
    }

	@Override
	public Plugin getPlugin(String pluginName) {
		if(checkInit() && (MetricKafkaClientPlugin.class.getName()).equals(pluginName)) {
			return this;
		}
		throw new RuntimeException("#MetricKafkaClientPlugin# pluginName: " + pluginName + " is no matched");
	}

}
