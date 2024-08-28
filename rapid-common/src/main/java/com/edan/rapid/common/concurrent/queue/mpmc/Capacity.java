package com.edan.rapid.common.concurrent.queue.mpmc;

/**
 * <B>主类名称：</B>Capacity<BR>
 * <B>概要说明：</B>Power Of 2容量效验器<BR>
 * @author edan
 * @since 2021年12月7日 上午11:18:46
 */
final class Capacity {

    public static final int MAX_POWER2 = (1<<30);

    public static int getCapacity(int capacity) {
        int c = 1;
        if(capacity >= MAX_POWER2) {
            c = MAX_POWER2;
        } else {
            while(c < capacity) c <<= 1;
        }

        if(isPowerOf2(c)) {
            return c;
        } else {
            throw new RuntimeException("Capacity is not a power of 2.");
        }
    }

    private static final boolean isPowerOf2(final int p) {
        // thanks mcheng for the suggestion
        return (p & (p - 1)) == 0;
    }
    
}