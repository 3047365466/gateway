package com.edan.rapid.core.plugin;

/**
 * <B>主类名称：</B>Plugin<BR>
 * <B>概要说明：</B>插件的生命周期管理<BR>
 * @author JiFeng
 * @since 2021年12月21日 上午1:43:41
 */
public interface Plugin {

    default boolean check() {
        return true;
    }

    void init();

    void destroy();
    
    Plugin getPlugin(String pluginName);

}
