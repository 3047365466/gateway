package com.edan.rapid.core.netty.processor;

import com.edan.rapid.core.context.HttpRequestWrapper;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/26 3:31
 */
public interface NettyProcessor {
    void process(HttpRequestWrapper httpRequestWrapper) throws Exception;

    void start();

    void shutdown();
}
