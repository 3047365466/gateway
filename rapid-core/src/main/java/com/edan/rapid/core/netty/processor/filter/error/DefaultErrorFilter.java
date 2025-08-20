package com.edan.rapid.core.netty.processor.filter.error;

import com.edan.rapid.common.constants.ProcessorFilterConstants;
import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.common.exception.RapidBaseException;
import com.edan.rapid.core.context.Context;
import com.edan.rapid.core.context.RapidResponse;
import com.edan.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.edan.rapid.core.netty.processor.filter.Filter;
import com.edan.rapid.core.netty.processor.filter.FilterConfig;
import com.edan.rapid.core.netty.processor.filter.ProcessorFilterType;

/**
 * <B>主类名称：</B>DefaultErrorFilter<BR>
 * <B>概要说明：</B>默认异常处理过滤器<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午7:47:25
 */
@Filter(
		id = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID,
		name = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_NAME,
		value = ProcessorFilterType.ERROR,
		order = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ORDER
		)
public class DefaultErrorFilter extends AbstractEntryProcessorFilter<FilterConfig> {

	public DefaultErrorFilter() {
		super(FilterConfig.class);
	}

	@Override
	public void entry(Context ctx, Object... args) throws Throwable {
		try {
			Throwable throwable = ctx.getThrowable();
			ResponseCode responseCode = ResponseCode.INTERNAL_ERROR;
			if(throwable instanceof RapidBaseException) {
				RapidBaseException rapidBaseException = (RapidBaseException)throwable;
				responseCode = rapidBaseException.getCode();
			}
			RapidResponse rapidResponse = RapidResponse.buildRapidResponse(responseCode);
			ctx.setResponse(rapidResponse);
		} finally {
			System.err.println("============> do error filter <===============");
			//	设置写回标记
			ctx.writtened();
			//	触发后面的过滤器执行
			super.fireNext(ctx, args);
		}
	}

}
