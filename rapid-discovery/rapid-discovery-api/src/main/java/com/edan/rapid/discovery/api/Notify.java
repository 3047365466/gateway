package com.edan.rapid.discovery.api;

/**
 * <B>主类名称：</B>Notify<BR>
 * <B>概要说明：</B>监听服务接口<BR>
 * @author JiFeng
 * @since 2021年12月19日 下午1:07:52
 */
public interface Notify {

	/**
	 * <B>方法名称：</B>put<BR>
	 * <B>概要说明：</B>添加或者更新的方法<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:28:32
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	void put(String key, String value) throws Exception;
	
	/**
	 * <B>方法名称：</B>delete<BR>
	 * <B>概要说明：</B>删除方法<BR>
	 * @author JiFeng
	 * @since 2021年12月19日 下午1:28:59
	 * @param key
	 * @throws Exception
	 */
	void delete(String key) throws Exception;
	
	
}
