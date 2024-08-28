package com.edan.rapid.core;

/**
 * @Description 声明生命周期接口
 * @Author Edan
 * @Create 2024/8/21 23:50
 */
public interface LifeCycle {
    /**
     *  声明周期初始化方法
     */
    void init();

    /**
     * 声明周期启动方法
     */
    void start();

    /**
     * 声明周期停止方法
     */
    void shutdown();
}
