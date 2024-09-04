package com.edan.rapid.core.netty.processor.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <B>主类名称：</B>DefaultCacheManager<BR>
 * <B>概要说明：</B>DefaultCacheManager<BR>
 * @author JiFeng
 * @since 2021年12月17日 上午12:02:47
 */
public class DefaultCacheManager {

	private DefaultCacheManager() {
	}
	
	public static final String FILTER_CONFIG_CACHE_ID = "filterConfigCache";
	
	//	这个是全局的缓存：双层缓存
	private final ConcurrentHashMap<String, Cache<String, ?>> cacheMap = new ConcurrentHashMap<>();

	private static class SingletonHolder {
		private static final DefaultCacheManager INSTANCE = new DefaultCacheManager();
	}
	
	public static DefaultCacheManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * <B>方法名称：</B>create<BR>
	 * <B>概要说明：</B>根据一个全局的缓存ID 创建一个Caffeine缓存对象<BR>
	 * @author JiFeng
	 * @since 2021年12月17日 上午12:09:29
	 * @param <V>
	 * @param cacheId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <V> Cache<String, V> create(String cacheId) {
		Cache<String, V> cache = Caffeine.newBuilder().build();
		cacheMap.put(cacheId, cache);
		return (Cache<String, V>) cacheMap.get(cacheId);
	}
	
	/**
	 * <B>方法名称：</B>remove<BR>
	 * <B>概要说明：</B>根据cacheId 和对应的真正Caffeine缓存key 删除一个Caffeine缓存对象<BR>
	 * @author JiFeng
	 * @since 2021年12月17日 上午12:11:17
	 * @param <V>
	 * @param cacheId
	 * @param key
	 */
	public <V> void remove(String cacheId, String key) {
		@SuppressWarnings("unchecked")
		Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
		if(cache != null) {
			cache.invalidate(key);
		}
	}
	
	/**
	 * <B>方法名称：</B>remove<BR>
	 * <B>概要说明：</B>根据全局的缓存id 删除这个Caffeine缓存对象<BR>
	 * @author JiFeng
	 * @since 2021年12月17日 上午12:12:08
	 * @param <V>
	 * @param cacheId
	 */
	public <V> void remove(String cacheId) {
		@SuppressWarnings("unchecked")
		Cache<String, V> cache = (Cache<String, V>) cacheMap.get(cacheId);
		if(cache != null) {
			cache.invalidateAll();
		}
	}
	/**
	 * <B>方法名称：</B>cleanAll<BR>
	 * <B>概要说明：</B>清空所有的缓存<BR>
	 * @author JiFeng
	 * @since 2021年12月17日 上午12:13:13
	 */
	public void cleanAll() {
		cacheMap.values().forEach(cache -> cache.invalidateAll());
	}
	
}
