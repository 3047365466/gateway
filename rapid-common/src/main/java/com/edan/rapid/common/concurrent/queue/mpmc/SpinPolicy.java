package com.edan.rapid.common.concurrent.queue.mpmc;
/**
 * <B>主类名称：</B>SpinPolicy<BR>
 * <B>概要说明：</B><BR>
 * @author edan
 * @since 2020年9月5日 下午5:43:52
 */
public enum SpinPolicy {
    WAITING,
    BLOCKING,
    SPINNING;
}