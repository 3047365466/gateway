package com.edan.rapid.common.concurrent.queue.mpmc;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <B>主类名称：</B>MpmcBlockingQueue<BR>
 * <B>概要说明：</B>多生产者多消费者阻塞队列<BR>
 * @author edan
 * @since 2021年12月7日 上午11:25:44
 */
public final class MpmcBlockingQueue<E> extends MpmcConcurrentQueue<E> implements Serializable, Iterable<E>, Collection<E>, BlockingQueue<E>, Queue<E>, ConcurrentQueue<E> {

	private static final long serialVersionUID = -5010414655037152451L;
	
	//	queueNotFullCondition: 如果队列是满那么就阻塞
    protected final Condition queueNotFullCondition;
    //	queueNotEmptyConditio: 如果队列是空的那么就
    protected final Condition queueNotEmptyCondition;

    public MpmcBlockingQueue(final int capacity) {
        // waiting locking gives substantial performance improvements
        // but makes disruptor aggressive with cpu utilization
        this(capacity, SpinPolicy.WAITING);
    }

    public MpmcBlockingQueue(final int capacity, final SpinPolicy spinPolicy) {
        super(capacity);
        switch(spinPolicy) {
            case BLOCKING:
                queueNotFullCondition = new QueueNotFull();
                queueNotEmptyCondition = new QueueNotEmpty();
                break;
            case SPINNING:
                queueNotFullCondition = new SpinningQueueNotFull();
                queueNotEmptyCondition = new SpinningQueueNotEmpty();
                break;
            case WAITING:
            default:
                queueNotFullCondition = new WaitingQueueNotFull();
                queueNotEmptyCondition = new WaitingQueueNotEmpty();
        }
    }

    public MpmcBlockingQueue(final int capacity, Collection<? extends E> c) {
        this(capacity);
        for (final E e : c) {
            offer(e);
        }
    }
    @Override
    public final boolean offer(E e) {
        if (super.offer(e)) {
            queueNotEmptyCondition.signal();
            return true;
        } else {
            queueNotEmptyCondition.signal();
            return false;
        }
    }

    @Override
    public final E poll() {
        final E e = super.poll();
        // not full now
        queueNotFullCondition.signal();
        return e;
    }

    @Override
    public int remove(final E[] e) {
        final int n = super.remove(e);
        // queue can not be full
        queueNotFullCondition.signal();
        return n;
    }

    @Override
    public E remove() {
        return poll();
    }

    @Override
    public E element() {
        final E val = peek();
        if (val != null)
            return val;
        throw new NoSuchElementException("No element found.");
    }

    /**
     * <B>方法名称：</B>put<BR>
     * <B>概要说明：</B>等待<BR>
     * @author  edan
     * @since 2021年12月8日 下午11:54:39
     * @see BlockingQueue#put(Object)
     */
    @Override
    public void put(E e) throws InterruptedException {
        // add object, wait for space to become available
        while (offer(e) == false) {
            if(Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            //	就等待
            queueNotFullCondition.await();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        for (;;) {
            if (offer(e)) {
                return true;
            } else {

                // wait for available capacity and try again
                if (!Condition.waitStatus(timeout, unit, queueNotFullCondition)) return false;
            }
        }
    }

    /**
     * <B>方法名称：</B>take<BR>
     * <B>概要说明：</B>等待<BR>
     * @author  edan
     * @since 2021年12月8日 下午11:54:56
     * @see BlockingQueue#take()
     */
    @Override
    public E take() throws InterruptedException {
        for (;;) {
            E pollObj = poll();
            if (pollObj != null) {
                return pollObj;
            }
            if(Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            queueNotEmptyCondition.await();
        }
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        for(;;) {
            E pollObj = poll();
            if(pollObj != null) {
                return pollObj;
            } else {
                // wait for the queue to have at least one element or time out
                if(!Condition.waitStatus(timeout, unit, queueNotEmptyCondition)) return null;
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        queueNotFullCondition.signal();
    }

    @Override
    public int remainingCapacity() {
        return size - size();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, size());
    }

    @Override
    // drain the whole queue at once
    public int drainTo(Collection<? super E> c, int maxElements) {

        // required by spec
        if (this == c) throw new IllegalArgumentException("Can not drain to self.");

        // batch remove is not supported in MPMC
        int nRead = 0;

        while(!isEmpty() && maxElements > 0) {
            final E e = poll();
            if(e != null) {
                c.add(e);
                nRead++;
            }
        }
        // only return the number that was actually added to the collection
        return nRead;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Object[] toArray() {
        final E[] e = (E[]) new Object[size()];
        toArray(e);
        return e;

    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T[] toArray(T[] a) {
        remove((E[]) a);
        return a;
    }

    @Override
    public boolean add(E e) {
        if (offer(e)) return true;
        throw new IllegalStateException("queue is full");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (final Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (final E e : c) {
            if (!offer(e)) return false;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new RingIter();
    }

   
    private final boolean isFull() {
//        final long queueStart = tail.get() - size;
//        return head.get() == queueStart; 
    	//	16
    	//  32 - 16 = 16
        return tail.get() - head.get() == size;
    }

    private final class RingIter implements Iterator<E> {
        int dx = 0;

        E lastObj = null;

        private RingIter() {
        }

        @Override
        public boolean hasNext() {
            return dx < size();
        }

        @Override
        public E next() {
            final long pollPos = head.get();
            final int slot = (int) ((pollPos + dx++) & mask);
            lastObj = buffer[slot].entry;
            return lastObj;
        }

        @Override
        public void remove() {
            MpmcBlockingQueue.this.remove(lastObj);
        }
    }

    /**
     * <B>主类名称：</B>QueueNotFull<BR>
     * <B>概要说明：</B>condition used for signaling queue is full<BR>
     * @author edan
     * @since 2020年9月5日 下午5:52:49
     */
    private final class QueueNotFull extends ConditionAbstract {

    	/**
    	 * <B>方法名称：</B>test<BR>
    	 * <B>概要说明：</B>test isFull<BR>
    	 * @author edan
    	 * @since 2020年9月5日 下午5:53:00
    	 * @see com.frxs.fd.common.concurrent.collection.Condition#test()
    	 * @return boolean - true if the queue is full
    	 */
        @Override
        public final boolean test() {
            return isFull();
        }
    }

    /**
     * <B>主类名称：</B>QueueNotEmpty<BR>
     * <B>概要说明：</B>condition used for signaling queue is empty<BR>
     * @author edan
     * @since 2020年9月5日 下午5:52:40
     */
    private final class QueueNotEmpty extends ConditionAbstract {
    	
    	/**
    	 * <B>方法名称：</B>test<BR>
    	 * <B>概要说明：</B>test isEmpty<BR>
    	 * @author edan
    	 * @since 2020年9月5日 下午5:52:24
    	 * @see com.frxs.fd.common.concurrent.collection.Condition#test()
    	 * @return boolean - true if the queue is empty
    	 */
        @Override
        public final boolean test() {
            return isEmpty();
        }
    }

    /**
     * <B>主类名称：</B>WaitingQueueNotFull<BR>
     * <B>概要说明：</B>condition used for signaling queue is full<BR>
     * @author edan
     * @since 2020年9月5日 下午5:52:17
     */
    private final class WaitingQueueNotFull extends ConditionAbstractWaiting {

    	/**
    	 * <B>方法名称：</B>test<BR>
    	 * <B>概要说明：</B>test isFull<BR>
    	 * @author edan
    	 * @since 2020年9月5日 下午5:51:54
    	 * @see com.frxs.fd.common.concurrent.collection.ConditionAbstractWaiting#test()
    	 * @return boolean - true if the queue is full
    	 */
        @Override
        public final boolean test() {
            return isFull();
        }
    }

    /**
     * <B>主类名称：</B>WaitingQueueNotEmpty<BR>
     * <B>概要说明：</B>condition used for signaling queue is empty<BR>
     * @author edan
     * @since 2020年9月5日 下午5:51:11
     */
    private final class WaitingQueueNotEmpty extends ConditionAbstractWaiting {
    	
    	/**
    	 * <B>方法名称：</B>test<BR>
    	 * <B>概要说明：</B>test isEmpty<BR>
    	 * @author edan
    	 * @since 2020年9月5日 下午5:50:51
    	 * @see com.frxs.fd.common.concurrent.collection.ConditionAbstractWaiting#test()
    	 * @return boolean - true if the queue is empty
    	 */
        @Override
        public final boolean test() {
            return isEmpty();
        }
    }

    /**
     * <B>主类名称：</B>SpinningQueueNotFull<BR>
     * <B>概要说明：</B>condition used for signaling queue is not full<BR>
     * @author edan
     * @since 2020年9月5日 下午5:51:20
     */
    private final class SpinningQueueNotFull extends ConditionAbstractSpinning {
    	
    	/**
    	 * <B>方法名称：</B>test<BR>
    	 * <B>概要说明：</B>test isFull<BR>
    	 * @author edan
    	 * @since 2020年9月5日 下午5:49:52
    	 * @see com.frxs.fd.common.concurrent.collection.Condition#test()
    	 * @return boolean - true if the queue is full
    	 */
        @Override
        public final boolean test() {
            return isFull();
        }
    }

    /**
     * <B>主类名称：</B>SpinningQueueNotEmpty<BR>
     * <B>概要说明：</B>condition used for signaling queue is empty<BR>
     * @author edan
     * @since 2020年9月5日 下午5:50:39
     */
    private final class SpinningQueueNotEmpty extends ConditionAbstractSpinning {
        
    	/**
         * <B>方法名称：</B>test<BR>
         * <B>概要说明：</B>test isEmpty<BR>
         * @author edan
         * @since 2020年9月5日 下午5:50:09
         * @see com.frxs.fd.common.concurrent.collection.Condition#test()
         * @return boolean - true if the queue is empty
         */
    	@Override
        public final boolean test() {
            return isEmpty();
        }
    }

}