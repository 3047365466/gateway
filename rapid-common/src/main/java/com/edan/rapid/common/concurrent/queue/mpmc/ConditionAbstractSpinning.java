package com.edan.rapid.common.concurrent.queue.mpmc;

/**
 * <B>主类名称：</B>ConditionAbstractSpinning<BR>
 * <B>概要说明：</B>阻塞的自旋锁抽象类<BR>
 * @author edan
 * @since 2021年12月7日 上午11:23:01
 */
public abstract class ConditionAbstractSpinning implements Condition {

	/**
	 * <B>方法名称：</B>awaitNanos<BR>
	 * <B>概要说明：</B>on spinning waiting breaking on test and expires > timeNow<BR>
	 * @author  edan
	 * @since 2021年12月7日 上午11:23:13
	 * @see com.edan.rapid.common.concurrent.queue.mpmc.Condition#awaitNanos(long)
	 */
    @Override
    public void awaitNanos(final long timeout) throws InterruptedException {
        long timeNow = System.nanoTime();
        final long expires = timeNow+timeout;

        final Thread t = Thread.currentThread();

        while(test() && expires > timeNow && !t.isInterrupted()) {
            timeNow = System.nanoTime();
            Condition.onSpinWait();
        }

        if(t.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    /**
     * <B>方法名称：</B>await<BR>
     * <B>概要说明：</B>on spinning waiting breaking on test<BR>
     * @author  edan
     * @since 2021年12月7日 上午11:23:38
     * @see com.edan.rapid.common.concurrent.queue.mpmc.Condition#await()
     */
    @Override
    public void await() throws InterruptedException {
        final Thread t = Thread.currentThread();

        while(test() && !t.isInterrupted()) {
            Condition.onSpinWait();
        }

        if(t.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    @Override
    public void signal() {

    }
}
