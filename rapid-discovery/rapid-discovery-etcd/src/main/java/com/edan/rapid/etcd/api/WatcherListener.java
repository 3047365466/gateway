package com.edan.rapid.etcd.api;

/**
 * <B>主类名称：</B>WatcherListener<BR>
 * <B>概要说明：</B>WatcherListener<BR>
 * @author JiFeng
 * @since 2021年12月19日 上午11:56:58
 */
public interface WatcherListener {

    void watcherKeyChanged(EtcdClient etcdClient, EtcdChangedEvent event) throws Exception;

}
