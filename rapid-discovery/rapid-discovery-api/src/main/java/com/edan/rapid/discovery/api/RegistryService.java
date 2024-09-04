package com.edan.rapid.discovery.api;

/**
 * <B>主类名称：</B>RegistryService<BR>
 * <B>概要说明：</B>注册服务接口<BR>
 * @author JiFeng
 * @since 2021年12月19日 下午1:07:25
 */
public interface RegistryService extends Registry {

	/**
	 * <B>方法名称：</B>addWatcherListeners<BR>
	 * <B>概要说明：</B>添加一堆的监听事件<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:29:50
	 * @param superPath 父节点目录
	 * @param notify 监听函数
	 */
	void addWatcherListeners(String superPath, Notify notify);

	/**
	 * <B>方法名称：</B>initialized<BR>
	 * <B>概要说明：</B>初始化注册服务<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午2:10:01
	 * @param registryAddress 注册服务地址
	 */
	void initialized(String registryAddress);
	
}
