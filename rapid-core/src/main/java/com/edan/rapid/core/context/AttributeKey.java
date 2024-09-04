package com.edan.rapid.core.context;

import com.edan.rapid.common.config.ServiceInvoker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <B>主类名称：</B>AttributeKey<BR>
 * <B>概要说明：</B>属性上下文的抽象类，在其内部进行实现<BR>
 * @author JiFeng
 * @since 2021年12月9日 上午10:49:36
 */
public abstract class AttributeKey<T> {

	private static final Map<String, AttributeKey<?>> namedMap = new HashMap<>();
	
	//	到负责均衡之前，要通过具体的服务，获取对应的服务实例列表
	public static final AttributeKey<Set<String>> MATCH_ADDRESS = create(Set.class);
	
	public static final AttributeKey<ServiceInvoker> HTTP_INVOKER = create(ServiceInvoker.class);

	public static final AttributeKey<ServiceInvoker> DUBBO_INVOKER = create(ServiceInvoker.class);

	static {
		namedMap.put("MATCH_ADDRESS", MATCH_ADDRESS);
		namedMap.put("HTTP_INVOKER", HTTP_INVOKER);
		namedMap.put("DUBBO_INVOKER", DUBBO_INVOKER);
	}
	
	public static AttributeKey<?> valueOf(String name) {
		return namedMap.get(name);
	}
	
	/**
	 * <B>方法名称：</B>cast<BR>
	 * <B>概要说明：</B>给我一个对象，转成对应的class类型<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午10:51:16
	 * @param value 真实的数据对象值
	 * @return 
	 */
	public abstract T cast(Object value);
	
	/**
	 * <B>方法名称：</B>create<BR>
	 * <B>概要说明：</B>对外暴露创建AttributeKey<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午10:58:30
	 * @param <T>
	 * @param valueClass 给我的对应的泛型类
	 * @return AttributeKey -> SimpleAttributeKey
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> AttributeKey<T> create(final Class<? super T> valueClass) {
		return new SimpleAttributeKey(valueClass);
	}
	
	/**
	 * <B>主类名称：</B>SimpleAttributeKey<BR>
	 * <B>概要说明：</B>简单的属性Key转换类<BR>
	 * @author JiFeng
	 * @since 2021年12月9日 上午10:55:21
	 */
	public static class SimpleAttributeKey<T> extends AttributeKey<T> {

		private final Class<T> valueClass;
		
		SimpleAttributeKey(final Class<T> valueClass) {
			this.valueClass = valueClass;
		}
		
		@Override
		public T cast(Object value) {
			return valueClass.cast(value);
		}
		
		@Override
		public String toString() {
			if(valueClass != null) {
				StringBuilder sb = new StringBuilder(getClass().getName());
				sb.append("<");
				sb.append(valueClass.getName());
				sb.append(">");
				return sb.toString();
			}
			return super.toString();
		}
		
	}
	
}
