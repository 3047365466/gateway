package com.edan.rapid.core.netty.processor.filter;

import com.edan.rapid.core.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <B>主类名称：</B>AbstractProcessorFilterFactory<BR>
 * <B>概要说明：</B>抽象的过滤器工厂<BR>
 * @author JiFeng
 * @since 2021年12月16日 上午12:20:53
 */
@Slf4j
public abstract class AbstractProcessorFilterFactory implements ProcessorFilterFactory {

	/*			
	 *	pre + route + post 
	 */
	public DefaultProcessorFilterChain defaultProcessorFilterChain = new DefaultProcessorFilterChain("defaultProcessorFilterChain"); 
	
	/*
	 * 	error + post
	 */
	public DefaultProcessorFilterChain errorProcessorFilterChain = new DefaultProcessorFilterChain("errorProcessorFilterChain"); 

	/*
	 * 	根据过滤器类型获取filter集合
	 */
	public Map<String /* processorFilterType */, Map<String, ProcessorFilter<Context>>> processorFilterTypeMap = new LinkedHashMap<>();
	
	/*
	 * 	根据过滤器id获取对应的Filter
	 */
	public Map<String /* filterId */, ProcessorFilter<Context>> processorFilterIdMap = new LinkedHashMap<>();
	
	/**
	 * <B>方法名称：</B>buildFilterChain<BR>
	 * <B>概要说明：</B>构建过滤器链条<BR>
	 * @author  JiFeng
	 * @since 2021年12月16日 上午12:44:00
	 * @see com.edan.edan.core.netty.processor.filter.ProcessorFilterFactory#buildFilterChain(com.edan.rapid.core.netty.processor.filter.ProcessorFilterType, List)
	 */
	@Override
	public void buildFilterChain(ProcessorFilterType filterType, List<ProcessorFilter<Context>> filters) throws Exception {
		switch (filterType) {
			case PRE:
			case ROUTE:
				addFilterForChain(defaultProcessorFilterChain, filters);
				break;
			case ERROR:
				addFilterForChain(errorProcessorFilterChain, filters);
				break;
			case POST:	
				addFilterForChain(defaultProcessorFilterChain, filters);
				addFilterForChain(errorProcessorFilterChain, filters);
			default:
				throw new RuntimeException("ProcessorFilterType is not supported !");
			}
		
	}
	
	private void addFilterForChain(DefaultProcessorFilterChain processorFilterChain,
			List<ProcessorFilter<Context>> filters) throws Exception {
		for(ProcessorFilter<Context> processorFilter : filters) {
			processorFilter.init();
			doBuilder(processorFilterChain, processorFilter);
		}
	}

	/**
	 * <B>方法名称：</B>doBuilder<BR>
	 * <B>概要说明：</B>添加过滤器到指定的filterChain<BR>
	 * @author JiFeng
	 * @since 2021年12月16日 上午12:46:15
	 * @param defaultProcessorFilterChain
	 * @param processorFilter
	 */
	private void doBuilder(DefaultProcessorFilterChain processorFilterChain,
			ProcessorFilter<Context> processorFilter) {
		
		log.info("filterChain: {}, the scanner filter is : {}", processorFilterChain.getId(), processorFilter.getClass().getName());
		
		Filter annotation = processorFilter.getClass().getAnnotation(Filter.class);
		
		if(annotation != null) {
			//	构建过滤器链条，添加filter
			processorFilterChain.addLast((AbstractLinkedProcessorFilter<Context>)processorFilter);
			
			//	映射到过滤器集合
			String filterId = annotation.id();
			if(filterId == null || filterId.length() < 1) {
				filterId = processorFilter.getClass().getName();
			}
			String code = annotation.value().getCode();
			Map<String, ProcessorFilter<Context>> filterMap = processorFilterTypeMap.get(code);
			if(filterMap == null) {
				filterMap = new LinkedHashMap<String, ProcessorFilter<Context>>();
			}
			filterMap.put(filterId, processorFilter);
			
			//	type
			processorFilterTypeMap.put(code, filterMap);
			//	id
			processorFilterIdMap.put(filterId, processorFilter);
		}
		
	}

	public <T> T getFilter(Class<T> t) throws Exception {
		Filter annotation = t.getAnnotation(Filter.class);
		if(annotation != null) {
			String filterId = annotation.id();
			if(filterId == null || filterId.length() < 1) {
				filterId = t.getName();
			}
			return this.getFilter(filterId);
		}
		return null;
	}
	

	@SuppressWarnings("unchecked")
	public <T> T getFilter(String filterId) throws Exception {
		ProcessorFilter<Context> filter = null;
		if(!processorFilterIdMap.isEmpty()) {
			filter = processorFilterIdMap.get(filterId);
		}
		return (T)filter;
	}
	
}
