package com.edan.rapid.core.netty.processor.filter.pre;

import com.edan.rapid.common.config.DubboServiceInvoker;
import com.edan.rapid.common.constants.ProcessorFilterConstants;
import com.edan.rapid.common.constants.RapidProtocol;
import com.edan.rapid.core.context.AttributeKey;
import com.edan.rapid.core.context.Context;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.context.RapidRequest;
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
    public void entry(Context ctx, Object... args) throws Throwable {
        try {
            RapidContext rapidContext = (RapidContext) ctx;
            String protocol = rapidContext.getProtocol();
            TimeoutPreFilter.Config config = (TimeoutPreFilter.Config) args[0];
            switch (protocol) {
                case RapidProtocol.HTTP:
                    RapidRequest rapidRequest = rapidContext.getRequest();
                    rapidRequest.setRequestTimeout(config.getTimeout());
                    break;
                case RapidProtocol.DUBBO:
                    DubboServiceInvoker dubboServiceInvoker = (DubboServiceInvoker)rapidContext.getRequiredAttribute(AttributeKey.DUBBO_INVOKER);
                    dubboServiceInvoker.setTimeout(config.getTimeout());
                    break;
                default:
                    break;
            }
        } finally {
            //	非常重要的，一定要记得：驱动我们的过滤器链表
            super.fireNext(ctx, args);
        }
    }

    @Getter
    @Setter
    public static class Config extends FilterConfig {
        private Integer timeout;

    }
}
