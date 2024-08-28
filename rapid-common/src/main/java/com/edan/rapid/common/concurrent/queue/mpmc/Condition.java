package com.edan.rapid.common.concurrent.queue.mpmc;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * <B>主类名称：</B>Condition<BR>
 * <B>概要说明：</B>Return true once a condition is satisfied<BR>
 * @author edan
 * @since 2021年12月7日 上午11:19:54
 */
interface Condition {

    long PARK_TIMEOUT = 50L;

    int MAX_PROG_YIELD = 2000;

    /**
     * <B>方法名称：</B>test<BR>
     * <B>概要说明：</B>return true if the queue condition is satisfied<BR>
     * @author edan
     * @since 2021年12月7日 上午11:20:07
     * @return
     */
    boolean test();

    /**
     * <B>方法名称：</B>awaitNanos<BR>
     * <B>概要说明：</B>wake me when the condition is satisfied, or timeout<BR>
     * @author edan
     * @since 2021年12月7日 上午11:20:18
     * @param timeout 超时时间
     * @throws InterruptedException
     */
    void awaitNanos(final long timeout) throws InterruptedException;

    /**
     * <B>方法名称：</B>await<BR>
     * <B>概要说明：</B>wake if signal is called, or wait indefinitely<BR>
     * @author edan
     * @since 2021年12月7日 上午11:20:32
     * @throws InterruptedException
     */
    void await() throws InterruptedException;

    /**
     * <B>方法名称：</B>signal<BR>
     * <B>概要说明：</B>tell threads waiting on condition to wake up<BR>
     * @author edan
     * @since 2021年12月7日 上午11:20:42
     */
    void signal();

    /**
     * <B>方法名称：</B>progressiveYield<BR>
     * <B>概要说明：</B>progressively transition from spin to yield over time<BR>
     * @author edan
     * @since 2021年12月7日 上午11:20:52
     * @param n
     * @return
     */
    static int progressiveYield(final int n) {
        if(n > 500) {
            if(n<1000) {
                // "randomly" yield 1:8
                if((n & 0x7) == 0) {
                    LockSupport.parkNanos(PARK_TIMEOUT);
                } else {
                    onSpinWait();
                }
            } else if(n<MAX_PROG_YIELD) {
                // "randomly" yield 1:4
                if((n & 0x3) == 0) {
                    Thread.yield();
                } else {
                    onSpinWait();
                }
            } else {
                Thread.yield();
                return n;
            }
        } else {
            onSpinWait();
        }
        return n+1;
    }

    static void onSpinWait() {
        // Java 9 hint for spin waiting PAUSE instruction
        //http://openjdk.java.net/jeps/285
        // Thread.onSpinWait();
    }

    /**
     * <B>方法名称：</B>waitStatus<BR>
     * <B>概要说明：</B>Wait for timeout on condition<BR>
     * @author edan
     * @since 2021年12月7日 上午11:21:23
     * @param timeout
     * @param unit
     * @param condition
     * @return
     * @throws InterruptedException
     */
    static boolean waitStatus(final long timeout, final TimeUnit unit, final Condition condition) throws InterruptedException {
        // until condition is signaled
        final long timeoutNanos = TimeUnit.NANOSECONDS.convert(timeout, unit);
        final long expireTime = System.nanoTime() + timeoutNanos;
        // the queue is empty or full wait for something to change
        while (condition.test()) {
            final long now = System.nanoTime();
            if (now > expireTime) {
                return false;
            }
            condition.awaitNanos(expireTime - now - PARK_TIMEOUT);
        }
        return true;
    }

}