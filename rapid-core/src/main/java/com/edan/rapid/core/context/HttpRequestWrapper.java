package com.edan.rapid.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/26 3:27
 */
@Data
public class HttpRequestWrapper {

    private FullHttpRequest fullHttpRequest;

    private ChannelHandlerContext ctx;
}
