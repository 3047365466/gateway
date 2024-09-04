package com.edan.rapid.etcd.api;

import io.etcd.jetcd.KeyValue;

/**
 * <B>主类名称：</B>EtcdChangedEvent<BR>
 * <B>概要说明：</B>EtcdChangedEvent<BR>
 * @author JiFeng
 * @since 2021年12月19日 上午11:55:24
 */
public class EtcdChangedEvent {

	public static enum Type {
        PUT,
        DELETE,
        UNRECOGNIZED;
    }
	
	private KeyValue prevKeyValue;
	
	private KeyValue curtkeyValue;

    private Type type;

    public EtcdChangedEvent(KeyValue prevKeyValue, KeyValue curtkeyValue, Type type) {
    	this.prevKeyValue = prevKeyValue;
        this.curtkeyValue = curtkeyValue;
        this.type = type;
    }

    public KeyValue getCurtkeyValue() {
		return curtkeyValue;
	}

	public KeyValue getPrevKeyValue() {
		return prevKeyValue;
	}

	public Type getType() {
        return type;
    }
    
}