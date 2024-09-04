package com.edan.rapid.etcd.api;

/**
 * <B>主类名称：</B>EtcdClientNotInitException<BR>
 * <B>概要说明：</B>EtcdClientNotInitException<BR>
 * @author JiFeng
 * @since 2021年12月19日 上午11:55:49
 */
public class EtcdClientNotInitException extends RuntimeException {

	private static final long serialVersionUID = -617743243793838282L;
	
	public EtcdClientNotInitException() {
		super();
	}
	
	public EtcdClientNotInitException(String message) {
		super(message);
	}

}
