package com.edan.rapid.common.concurrent.queue.mpmc;

/**
 * <B>主类名称：</B>Contended<BR>
 * <B>概要说明：</B>Linux Intel CacheLine Size 64<BR>
 * @author edan
 * @since 2021年12月7日 上午11:24:57
 */
public class Contended {

    public static final int CACHE_LINE = Integer.getInteger("Intel.CacheLineSize", 64); // bytes

}
