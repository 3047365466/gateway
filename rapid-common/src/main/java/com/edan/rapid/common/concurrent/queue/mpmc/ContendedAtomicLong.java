package com.edan.rapid.common.concurrent.queue.mpmc;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * <B>主类名称：</B>ContendedAtomicLong<BR>
 * <B>概要说明：</B>Avoid false cache line sharing<BR>
 * @author edan
 * @since 2021年12月7日 上午11:25:13
 */
public class ContendedAtomicLong extends Contended {

	//	一个缓存行需要多少个Long元素的填充：8
    private static final int CACHE_LINE_LONGS = CACHE_LINE / Long.BYTES;

    private final AtomicLongArray contendedArray;

    //	77
    ContendedAtomicLong(final long init)
    {
        contendedArray = new AtomicLongArray(2 * CACHE_LINE_LONGS);
        set(init);
    }

    void set(final long l) {
        contendedArray.set(CACHE_LINE_LONGS, l);
    }

    long get() {
        return contendedArray.get(CACHE_LINE_LONGS);
    }

    public String toString() {
        return Long.toString(get());
    }

    public boolean compareAndSet(final long expect, final long l) {
        return contendedArray.compareAndSet(CACHE_LINE_LONGS, expect, l);
    }
}
