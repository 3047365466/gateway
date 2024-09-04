package com.edan.rapid.discovery.api;

import com.edan.rapid.common.util.Pair;

import java.util.List;

/**
 * <B>主类名称：</B>Registry<BR>
 * <B>概要说明：</B>注册接口<BR>
 * @author JiFeng
 * @since 2021年12月19日 下午1:06:58
 */
public interface Registry {

	/**
	 * 	/services: 是要存储所有的服务定义信息的：ServiceDefinition (永久存储)
	 */
	String SERVICE_PREFIX = "/services";
	
	/**
	 * 	/instances: 是要存储所有的服务实例信息的： ServiceInstance (加载时存储)
	 */
	String INSTANCE_PREFIX = "/instances";
	
	/**
	 * 	/rules: 是要存储所有的规则信息的：Rule (永久存储)
	 */
	String RULE_PREFIX = "/rules";
	
	/**
	 * 	/gateway: 这个是要存储所有的网关本身自注册信息的： rapid-core(网关服务本身，加载时存储)
	 */
	String GATEWAY_PREFIX = "/gateway";
	
	String PATH = "/";
	
	/**
	 * <B>方法名称：</B>registerPathIfNotExists<BR>
	 * <B>概要说明：</B>注册一个路径如果不存在<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:22:26
	 * @param path
	 * @param value
	 * @param isPersistent
	 * @throws Exception
	 */
	void registerPathIfNotExists(String path, String value, boolean isPersistent) throws Exception;
	
	/**
	 * <B>方法名称：</B>registerEphemeralNode<BR>
	 * <B>概要说明：</B>注册一个临时节点<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:23:22
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	long registerEphemeralNode(String key, String value) throws Exception;
	
	/**
	 * <B>方法名称：</B>registerPersistentNode<BR>
	 * <B>概要说明：</B>注册一个永久节点<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:23:50
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	void registerPersistentNode(String key, String value) throws Exception;
	
	/**
	 * <B>方法名称：</B>getListByPrefixKey<BR>
	 * <B>概要说明：</B>通过一个前缀路径，获取一堆对应的数据信息<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:25:14
	 * @param prefix
	 * @return
	 */
	List<Pair<String, String>> getListByPrefixKey(String prefix) throws Exception;
	
	/**
	 * <B>方法名称：</B>getByKey<BR>
	 * <B>概要说明：</B>通过一个key查询对应键值对对象<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:26:57
	 * @param key
	 * @return
	 * @throws Exception
	 */
	Pair<String, String> getByKey(String key) throws Exception;
	
	/**
	 * <B>方法名称：</B>isExistKey<BR>
	 * <B>概要说明：</B>根据一个key键，判断是否存在<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:26:12
	 * @param key
	 * @return
	 * @throws Exception
	 */
	boolean isExistKey(String key) throws Exception;
	
	/**
	 * <B>方法名称：</B>deleteByKey<BR>
	 * <B>概要说明：</B>根据key删除<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:27:27
	 * @param key
	 */
	void deleteByKey(String key);
	
	/**
	 * <B>方法名称：</B>close<BR>
	 * <B>概要说明：</B>关闭服务<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:27:41
	 */
	void close();
	
}
