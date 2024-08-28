package com.edan.rapid.core;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/20 23:58
 */
public class Bootstrap {
    public static void main(String[] args) {
        //  1. 加载网关的配置信息
        RapidConfig rapidConfig = RapidConfigLoader.getInstance().load(args);
        // 2. 插件初始化的工作

        // 3. 初始化服务注册管理中心， 监听动态配置的新增、删除、修改

        // 4. 启动容器
        RapidContainer rapidContainer = new RapidContainer(rapidConfig);
        rapidContainer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                rapidContainer.shutdown();
            }
        }));
    }
}
