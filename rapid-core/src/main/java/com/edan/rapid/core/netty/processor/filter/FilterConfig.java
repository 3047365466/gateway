package com.edan.rapid.core.netty.processor.filter;

import lombok.Data;

/**
 * <B>主类名称：</B>FilterConfig<BR>
 * <B>概要说明：</B>所有的过滤器配置实现类的Base类<BR>
 * @author JiFeng
 * @since 2021年12月17日 上午12:28:13
 */
@Data
public class FilterConfig {
	
	/**
	 * 	是否打印日志
	 */
	private boolean loggable = false;

}
