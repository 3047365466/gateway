package com.edan.rapid.common.concurrent.queue.flusher;

/**
 * <B>主类名称：</B>Flusher<BR>
 * <B>概要说明：</B>Flusher接口定义<BR>
 * @author edan
 * @since 2021年12月7日 上午12:21:03
 */
public interface Flusher<E> {

    /**
     * <B>方法名称：</B>add<BR>
     * <B>概要说明：</B>添加元素方法<BR>
     * @author edan
     * @since 2021年12月7日 上午12:21:27
     * @param event
     */
    void add(E event);

    /**
     * <B>方法名称：</B>add<BR>
     * <B>概要说明：</B>添加多个元素<BR>
     * @author edan
     * @since 2021年12月7日 上午12:21:53
     * @param event
     */
    void add(@SuppressWarnings("unchecked") E... event);

    /**
     * <B>方法名称：</B>tryAdd<BR>
     * <B>概要说明：</B>尝试添加一个元素, 如果添加成功返回true 失败返回false<BR>
     * @author edan
     * @since 2021年12月7日 上午12:22:27
     * @param event
     * @return
     */
    boolean tryAdd(E event);

    /**
     * <B>方法名称：</B>tryAdd<BR>
     * <B>概要说明：</B>尝试添加多个元素, 如果添加成功返回true 失败返回false<BR>
     * @author edan
     * @since 2021年12月7日 上午12:23:04
     * @param event
     * @return
     */
    boolean tryAdd(@SuppressWarnings("unchecked")E... event);

    /**
     * <B>方法名称：</B>isShutdown<BR>
     * <B>概要说明：</B>isShutdown<BR>
     * @author edan
     * @since 2021年12月7日 上午12:23:43
     * @return
     */
    boolean isShutdown();

    /**
     * <B>方法名称：</B>start<BR>
     * <B>概要说明：</B>start<BR>
     * @author edan
     * @since 2021年12月7日 上午12:23:48
     */
    void start();

    /**
     * <B>方法名称：</B>shutdown<BR>
     * <B>概要说明：</B>shutdown<BR>
     * @author edan
     * @since 2021年12月7日 上午12:23:53
     */
    void shutdown();


}