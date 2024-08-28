package com.edan.rapid.core.netty;

import com.edan.rapid.core.context.HttpRequestWrapper;
import com.edan.rapid.core.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/26 2:21
 */
@Slf4j
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {

    private NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            // 分装请求，防止异步请求导致这里进行用时过久
            HttpRequestWrapper wrapper = new HttpRequestWrapper();
            wrapper.setFullHttpRequest(request);
            wrapper.setCtx(ctx);

            nettyProcessor.process(wrapper);

        } else {
            // 这一步是不会进行的
            log.error("");
            boolean release = ReferenceCountUtil.release(msg);
            if (!release) {
                log.error("");
            }
        }
    }
}
