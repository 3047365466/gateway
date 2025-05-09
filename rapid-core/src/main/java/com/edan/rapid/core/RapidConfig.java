package com.edan.rapid.core;

import com.edan.rapid.common.constants.BasicConst;
import com.edan.rapid.common.constants.RapidBufferHelper;
import com.edan.rapid.common.util.NetUtils;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import lombok.Data;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/21 0:49
 */
@Data
public class RapidConfig {
    //	网关的默认端口
    private int port = 8888;

    //	网关服务唯一ID： rapidId  192.168.11.111:8888
    private String rapidId = NetUtils.getLocalIp() + BasicConst.COLON_SEPARATOR + port;

    //	网关的注册中心地址
    private String registryAddress = "http://47.98.231.200:2379";

    //	网关的命名空间：dev test prod
    private String namespace = "rapid-dev";

    private String env = "dev";

    //	网关服务器的CPU核数映射的线程数
    private int processThread = Runtime.getRuntime().availableProcessors();

    // 	Netty的Boss线程数
    private int eventLoopGroupBossNum = 1;

    //	Netty的Work线程数
    private int eventLoopGroupWorkNum = processThread;

    //	是否开启EPOLL
    private boolean useEPoll = true;

    //	是否开启Netty内存分配机制
    private boolean nettyAllocator = true;

    //	http body报文最大大小
    private int maxContentLength = 64 * 1024 * 1024;

    //	dubbo开启连接数数量
    private int dubboConnections = processThread;

    //	设置响应模式, 默认是单异步模式：CompletableFuture回调处理结果： whenComplete  or  whenCompleteAsync
    private boolean whenComplete = true;

    //	网关队列配置：缓冲模式；
    private String bufferType = RapidBufferHelper.MPMC; // RapidBufferHelper.FLUSHER;

    //	网关队列：内存队列大小
    private int bufferSize = 1024 * 16;

    //	网关队列：阻塞/等待策略
    private String waitStrategy = "blocking";

    //	默认请求超时时间 3s
    private long requestTimeout = 3000;

    //	默认路由转发的慢调用时间 2s
    private long routeTimeout = 2000;

    //	kafka地址
    private String kafkaAddress = "";//"192.168.11.51:9092";

    //	网关服务指标消息主题
    private String metricTopic = "rapid-metric-topic";
    public WaitStrategy getTrueWaitStrategy() {
        switch (waitStrategy) {
            case "blocking" :
                return new BlockingWaitStrategy();
            case "busySpin":
                return new BusySpinWaitStrategy();
            case "yielding":
                return new YieldingWaitStrategy();
            case "sleeping":
                return new SleepingWaitStrategy();
            default:
                return new BlockingWaitStrategy();
        }
    }

    //	Http Async 参数选项：

    //	连接超时时间
    private int httpConnectTimeout = 30 * 1000;

    //	请求超时时间
    private int httpRequestTimeout = 30 * 1000;

    //	客户端请求重试次数
    private int httpMaxRequestRetry = 2;

    //	客户端请求最大连接数
    private int httpMaxConnections = 10000;

    //	客户端每个地址支持的最大连接数
    private int httpConnectionsPerHost = 8000;

    //	客户端空闲连接超时时间, 默认60秒
    private int httpPooledConnectionIdleTimeout = 60 * 1000;

    // Redis配置
    private String redisHost = "117.50.121.62";

    private int redisPort = 6379;

    private String redisPassword = "tdk2580TDK";

    private boolean redisEnabled = false;

    // Redis哨兵模式配置
    private boolean redisSentinelEnabled = true;

    private String redisMasterName = "master";

    // 哨兵节点列表，格式：host1:port1,host2:port2,host3:port3
    private String redisSentinels = "117.50.121.62:26379,111.229.152.195:26379,47.98.231.200:26379";

    private int redisDatabase = 0;

    // Getters and setters for rate limiting
    // 限流器配置

    private String rateLimiterType = "REDIS_SENTINEL"; // 可选值: LOCAL, REDIS, REDIS_SENTINEL

    // 全局限流相关配置

    private boolean limitEnabled = true;
    private String limitKeyPrefix = "rate_limit:";
    private String limitAlgorithm = "TOKEN_BUCKET";
    private int limitMaxTokens = 100;
    private int limitTokensPerSecond = 10;
    private int limitMaxRequests = 100;
    private int limitWindowSize = 60;
    private String limitMessage = "Too many requests, please try again later";

}
