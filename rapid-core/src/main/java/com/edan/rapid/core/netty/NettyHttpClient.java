package com.edan.rapid.core.netty;

import com.edan.rapid.core.LifeCycle;
import com.edan.rapid.core.RapidConfig;
import com.edan.rapid.core.helper.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/21 23:52
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {
    private AsyncHttpClient asyncHttpClient;

    private DefaultAsyncHttpClientConfig.Builder clientBuilder;

    private RapidConfig rapidConfig;

    private EventLoopGroup eventLoopGroupWork;

    public NettyHttpClient(RapidConfig rapidConfig, EventLoopGroup eventLoopGroupWork) {
        this.rapidConfig = rapidConfig;
        this.eventLoopGroupWork = eventLoopGroupWork;
        //	在构造函数调用初始化方法
        init();
    }


    @Override
    public void init() {
        this.clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
                .setFollowRedirect(false)
                .setEventLoopGroup(eventLoopGroupWork)
                .setConnectTimeout(rapidConfig.getHttpConnectTimeout())
                .setRequestTimeout(rapidConfig.getHttpRequestTimeout())
                .setMaxRequestRetry(rapidConfig.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setMaxConnections(rapidConfig.getHttpMaxConnections())
                .setMaxConnectionsPerHost(rapidConfig.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(rapidConfig.getHttpPooledConnectionIdleTimeout());
    }

    @Override
    public void start() {
        this.asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if(asyncHttpClient != null) {
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {
                // ignore
                log.error("#NettyHttpClient.shutdown# shutdown error", e);
            }
        }
    }
}
