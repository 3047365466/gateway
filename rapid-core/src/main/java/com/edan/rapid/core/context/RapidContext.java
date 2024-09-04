package com.edan.rapid.core.context;

import com.edan.rapid.common.config.Rule;
import com.edan.rapid.common.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/29 4:08
 */
public class RapidContext extends BasicContext{

    private final RapidRequest rapidRequest;

    private RapidResponse rapidResponse;

    private final Rule rule;

    private RapidContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive,
                         RapidRequest rapidRequest, Rule rule) {
        super(protocol, nettyCtx, keepAlive);
        this.rapidRequest = rapidRequest;
        this.rule = rule;
    }

    /**
     * <B>方法名称：</B>getRequiredAttribute<BR>
     * <B>概要说明：</B>获取必要的上下文参数，如果没有则抛出IllegalArgumentException<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:41:59
     * @param <T>
     * @param key 必须要存在的
     * @return T
     */
    public <T> T getRequiredAttribute(AttributeKey<T> key) {
        T value = getAttribute(key);
        AssertUtil.notNull(value, "required attribute '" + key + "' is missing !");
        return value;
    }

    /**
     * <B>方法名称：</B>getAttributeOrDefault<BR>
     * <B>概要说明：</B>获取指定key的上下文参数，如果没有则返回第二个参数的默认值<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:44:23
     * @param <T>
     * @param key
     * @param defaultValue
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttributeOrDefault(AttributeKey<T> key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * <B>方法名称：</B>getFilterConfig<BR>
     * <B>概要说明：</B>根据过滤器id获取对应的过滤器配置信息<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:46:00
     * @param filterId
     * @return Rule.FilterConfig
     */
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    /**
     * <B>方法名称：</B>getUniqueId<BR>
     * <B>概要说明：</B>获取上下文中唯一的UniqueId<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:46:53
     * @return
     */
    public String getUniqueId() {
        return rapidRequest.getUniqueId();
    }
    /**
     * <B>方法名称：</B>releaseRequest<BR>
     * <B>概要说明：</B>重写覆盖父类：basicContext的该方法，主要用于真正的释放操作<BR>
     * @author  JiFeng
     * @since 2021年12月9日 下午2:53:07
     * @see com.edan.rapid.core.context.BasicContext#releaseRequest()
     */
    @Override
    public void releaseRequest() {
        if(requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(rapidRequest.getFullHttpRequest());
        }
    }
    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public RapidRequest getRequest() {
        return rapidRequest;
    }

    /**
     * <B>方法名称：</B>getOriginRequest<BR>
     * <B>概要说明：</B>调用该方法就是获取原始请求内容，不去做任何修改动作<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:50:27
     * @return
     */
    public RapidRequest getOriginRequest() {
        return rapidRequest;
    }

    /**
     * <B>方法名称：</B>getRequestMutale<BR>
     * <B>概要说明：</B>调用该方法区分于原始的请求对象操作，主要就是做属性修改的<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:49:03
     * @return RapidRequest
     */
    public RapidRequest getRequestMutale() {
        return rapidRequest;
    }

    @Override
    public RapidResponse getResponse() {
        return rapidResponse;
    }

    @Override
    public void setResponse(Object response) {
        this.rapidResponse = (RapidResponse)response;
    }

    /**
     * <B>主类名称：</B>Builder<BR>
     * <B>概要说明：</B>建造者类<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:39:36
     */
    public static class Builder {

        private String protocol;

        private ChannelHandlerContext nettyCtx;

        private RapidRequest rapidRequest;

        private Rule rule;

        private boolean keepAlive;

        public Builder() {
        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setRapidRequest(RapidRequest rapidRequest) {
            this.rapidRequest = rapidRequest;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public RapidContext build() {
            AssertUtil.notNull(protocol, "protocol不能为空");
            AssertUtil.notNull(nettyCtx, "nettyCtx不能为空");
            AssertUtil.notNull(rapidRequest, "rapidRequest不能为空");
            AssertUtil.notNull(rule, "rule不能为空");
            return new RapidContext(protocol, nettyCtx, keepAlive, rapidRequest, rule);
        }
    }
}
