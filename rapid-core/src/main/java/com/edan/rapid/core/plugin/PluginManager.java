package com.edan.rapid.core.plugin;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * <B>主类名称：</B>PluginManager<BR>
 * <B>概要说明：</B>插件管理器<BR>
 * @author JiFeng
 * @since 2021年12月21日 上午1:44:01
 */
@Slf4j
public class PluginManager {

    private final MultiplePluginImpl multiplePlugin;

    private static final PluginManager INSTANCE = new PluginManager();

    public static Plugin getPlugin() {
        return INSTANCE.multiplePlugin;
    }

    private PluginManager() {
        //	SPI方式扫描所有插件实现
        ServiceLoader<Plugin> plugins = ServiceLoader.load(Plugin.class);
        Map<String, Plugin> multiplePlugins = new HashMap<String, Plugin>();
        for (Plugin plugin : plugins) {
            if (!plugin.check()) {
            	continue;
            }
            String pluginName = plugin.getClass().getName();
            multiplePlugins.put(pluginName, plugin);
            log.info("#PluginFactory# The Scanner Plugin is: {}", plugin.getClass().getName());
        }
        //	安全执行插件逻辑
        this.multiplePlugin = new MultiplePluginImpl(multiplePlugins);
        Runtime.getRuntime().addShutdownHook(new Thread(multiplePlugin::destroy, "Shutdown-Plugin"));
    }

}
