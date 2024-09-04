package com.edan.rapid.core.netty.processor.filter;

import com.edan.rapid.core.context.Context;

import java.util.List;

/**
 * <B>主类名称：</B>ProcessorFilterFactory<BR>
 * <B>概要说明：</B>过滤器工厂接口<BR>
 * @author JiFeng
 * @since 2021年12月14日 上午2:06:05
 */
public interface ProcessorFilterFactory {

	/**
	 * <B>方法名称：</B>buildFilterChain<BR>
	 * <B>概要说明：</B>根据过滤器类型，添加一组过滤器，用于构建过滤器链<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午2:08:06
	 * @param filterType
	 * @param filters
	 * @throws Exception
	 */
	void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception;
	
	
	/**
	 * <B>方法名称：</B>doFilterChain<BR>
	 * <B>概要说明：</B>正常情况下执行过滤器链条<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午2:09:17
	 * @param ctx
	 * @throws Exception
	 */
	void doFilterChain(Context ctx) throws Exception;
	
	
	/**
	 * <B>方法名称：</B>doErrorFilterChain<BR>
	 * <B>概要说明：</B>错误、异常情况下执行该过滤器链条<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午2:10:01
	 * @param ctx
	 * @throws Exception
	 */
	void doErrorFilterChain(Context ctx) throws Exception;
	
	/**
	 * <B>方法名称：</B>getFilter<BR>
	 * <B>概要说明：</B>获取指定类类型的过滤器<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午2:11:16
	 * @param <T>
	 * @param t
	 * @return
	 * @throws Exception
	 */
	<T> T getFilter(Class<T> t) throws Exception;
	
	/**
	 * <B>方法名称：</B>getFilter<BR>
	 * <B>概要说明：</B>获取指定ID的过滤器<BR>
	 * @author JiFeng
	 * @since 2021年12月14日 上午2:11:58
	 * @param <T>
	 * @param filterId
	 * @return
	 * @throws Exception
	 */
	<T> T getFilter(String filterId) throws Exception;
	
	
}
