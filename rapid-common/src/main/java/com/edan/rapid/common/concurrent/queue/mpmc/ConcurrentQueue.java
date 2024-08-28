package com.edan.rapid.common.concurrent.queue.mpmc;

/**
 * <B>主类名称：</B>ConcurrentQueue<BR>
 * <B>概要说明：</B>A very high performance blocking buffer, based on Disruptor approach to queues<BR>
 * @author edan
 * @since 2021年12月7日 上午11:19:08
 */
public interface ConcurrentQueue<E> {

    boolean offer(E e);

    E poll();

    E peek();

    int size();

    int capacity();

    boolean isEmpty();

    boolean contains(Object o);

    int remove(E[] e);

    void clear();
    
}