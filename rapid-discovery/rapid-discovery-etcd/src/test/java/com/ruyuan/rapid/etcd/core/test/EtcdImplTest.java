package com.ruyuan.rapid.etcd.core.test;

import com.edan.rapid.etcd.api.EtcdClient;
import com.edan.rapid.etcd.core.EtcdClientImpl;
import io.etcd.jetcd.KeyValue;
import org.junit.Test;

import java.nio.charset.Charset;

public class EtcdImplTest {

	@Test
	public void test() throws Exception {
		
		String registryAddress = "http://47.98.231.200:2379";
		
		EtcdClient etcdClient = new EtcdClientImpl(registryAddress, true);
		
		System.err.println("etcdClient: " + etcdClient);
		
		KeyValue keyValue = etcdClient.getKey("/");
		
		System.err.println("key: " + keyValue.getKey().toString(Charset.defaultCharset()) + ", value: " + keyValue.getValue().toString(Charset.defaultCharset()));
	}
	
	
}
