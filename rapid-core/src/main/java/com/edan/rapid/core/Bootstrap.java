package com.edan.rapid.core;

import com.edan.rapid.core.discovery.RegistryManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/20 23:58
 */
@Slf4j
public class Bootstrap {
    public static void main(String[] args) {
        //  1. 加载网关的配置信息
        RapidConfig rapidConfig = RapidConfigLoader.getInstance().load(args);
        //	2. 插件初始化的工作
        // PluginManager.getPlugin().init();

        //	3. 初始化服务注册管理中心（服务注册管理器）, 监听动态配置的新增、修改、删除
        try {
            RegistryManager.getInstance().initialized(rapidConfig);
        } catch (Exception e) {
            log.error("RegistryManager is failed", e);
        }
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
