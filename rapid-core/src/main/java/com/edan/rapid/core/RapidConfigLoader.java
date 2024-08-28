package com.edan.rapid.core;

import com.edan.rapid.common.util.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;


@Slf4j
public class RapidConfigLoader {
	
	private final static String CONFIG_ENV_PREFIEX = "RAPID_";
	
	private final static String CONFIG_JVM_PREFIEX = "rapid.";
	
	private final static String CONFIG_FILE = "rapid.properties";
	
	private final static RapidConfigLoader INSTANCE = new RapidConfigLoader();
	
	private RapidConfig rapidConfig = new RapidConfig();
	
	private RapidConfigLoader() {
	}
	
	public static RapidConfigLoader getInstance() {
		return INSTANCE;
	}
	
	public static RapidConfig getRapidConfig() {
		return INSTANCE.rapidConfig;
	}
	
	public RapidConfig load(String args[]) {
		
		//	加载逻辑
		
		//	1. 配置文件
		{
			InputStream is = RapidConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
			if(is != null) {
				Properties properties = new Properties();
				try {
					properties.load(is);
					PropertiesUtils.properties2Object(properties, rapidConfig);
				} catch (IOException e) {
					//	warn
					log.warn("#RapidConfigLoader# load config file: {} is error", CONFIG_FILE, e);
				} finally {
					if(is != null) {
						try {
							is.close();
						} catch (IOException e) {
							//	ignore 
						}
					}
				}
			}
		}
		
		//	2. 环境变量
		{
			Map<String, String> env = System.getenv();
			Properties properties = new Properties();
			properties.putAll(env);
			PropertiesUtils.properties2Object(properties, rapidConfig, CONFIG_ENV_PREFIEX);
		}
		
		//	3. jvm参数
		{
			Properties properties = System.getProperties();
			PropertiesUtils.properties2Object(properties, rapidConfig, CONFIG_JVM_PREFIEX);
		}
		
		//	4. 运行参数: --xxx=xxx --enable=true  --port=1234
		{
			if(args != null && args.length > 0) {
				Properties properties = new Properties();
				for(String arg : args) {
					if(arg.startsWith("--") && arg.contains("=")) {
						properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
					}
				}
				PropertiesUtils.properties2Object(properties, rapidConfig);
			}
		}
		
		return rapidConfig;
	}

}
