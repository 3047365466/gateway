package com.edan.rapid.core.netty.processor.filter.pre;

import com.edan.rapid.common.constants.ProcessorFilterConstants;
import com.edan.rapid.core.context.Context;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.edan.rapid.core.netty.processor.filter.Filter;
import com.edan.rapid.core.netty.processor.filter.FilterConfig;
import com.edan.rapid.core.netty.processor.filter.ProcessorFilterType;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/30 7:54
 */
@Filter(
        id = "TimeoutPreFilter",
        name = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_NAME,
        value = ProcessorFilterType.PRE,
        order = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ORDER
)
public class TimeoutPreFilter extends AbstractEntryProcessorFilter<TimeoutPreFilter.Config> {


    public TimeoutPreFilter() {
        super(TimeoutPreFilter.Config.class);
    }

    @Override
    public void entry(Context context, Object... args) throws Throwable {
        RapidContext rapidContext = (RapidContext) context;

    }

    @Getter
    @Setter
    public static class Config extends FilterConfig {
        private Integer timeout;

    }
}
