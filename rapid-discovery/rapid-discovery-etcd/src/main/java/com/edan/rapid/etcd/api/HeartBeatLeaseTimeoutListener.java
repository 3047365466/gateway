package com.edan.rapid.etcd.api;

/**
 * <B>主类名称：</B>HeartBeatLeaseTimeoutListener<BR>
 * <B>概要说明：</B>HeartBeatLeaseTimeoutListener<BR>
 * @author edan
 * @since 2024年8月19日 上午11:56:27
 */
public interface HeartBeatLeaseTimeoutListener {
	
	void timeoutNotify();
	
}