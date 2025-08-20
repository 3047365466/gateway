package com.edan.rapid.core.helper;

import com.github.benmanes.caffeine.cache.Cache;
import com.edan.rapid.common.config.DubboServiceInvoker;
import com.edan.rapid.common.enums.ResponseCode;
import com.edan.rapid.common.exception.DubboConnectException;
import com.edan.rapid.core.RapidConfig;
import com.edan.rapid.core.RapidConfigLoader;
import com.edan.rapid.core.balance.DubboLoadBalance;
import com.edan.rapid.core.context.AttributeKey;
import com.edan.rapid.core.context.DubboRequest;
import com.edan.rapid.core.context.RapidContext;
import com.edan.rapid.core.netty.processor.cache.DefaultCacheManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.apache.dubbo.remoting.Constants.DISPATCHER_KEY;
import static org.apache.dubbo.rpc.protocol.dubbo.Constants.SHARE_CONNECTIONS_KEY;

/**
 * <B>主类名称：</B>DubboReferenceHelper<BR>
 * <B>概要说明：</B>泛化调用的辅助类<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午8:21:08
 */
@SuppressWarnings("all")
public class DubboReferenceHelper {

    public static final String DUBBO_TRANSFER_CONTEXT = "DUBBO_TRANSFER_CONTEXT";

    private static final String APPLICATION_CONFIG_NAME = "rapid-consumer";

    private static final String APPLICATION_OWNER = "rapid";

    private static final String APPLICATION_ORGANIZATION = "rapid";

    private static final int DEFAULT_TIMEOUT = 5000;

    private final ApplicationConfig applicationConfig;
    
    private volatile ReferenceConfigCache referenceConfigCache = ReferenceConfigCache.getCache();

    private final Cache<String, GenericService> cache = DefaultCacheManager.getInstance().createForDubboGenericService();

    private DubboReferenceHelper() {
        this.applicationConfig = new ApplicationConfig(APPLICATION_CONFIG_NAME);
        this.applicationConfig.setOwner(APPLICATION_OWNER);
        this.applicationConfig.setOrganization(APPLICATION_ORGANIZATION);
    }
    
    private enum Singleton {

        INSTANCE;

        private DubboReferenceHelper singleton;

        Singleton() {
            singleton = new DubboReferenceHelper();
        }

        public DubboReferenceHelper getInstance() {
            return singleton;
        }

    }

    public static DubboReferenceHelper getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    public static DubboRequest buildDubboRequest(DubboServiceInvoker dubboServiceInvoker, Object[] parameters) {
        DubboRequest dubboRequest = new DubboRequest();
        dubboRequest.setRegistriesStr(dubboServiceInvoker.getRegisterAddress());
        dubboRequest.setInterfaceClass(dubboServiceInvoker.getInterfaceClass());
        dubboRequest.setMethodName(dubboServiceInvoker.getMethodName());
        dubboRequest.setParameterTypes(dubboServiceInvoker.getParameterTypes());
        dubboRequest.setArgs(parameters);
        dubboRequest.setTimeout(dubboServiceInvoker.getTimeout());
        dubboRequest.setVersion(dubboServiceInvoker.getVersion());
        return dubboRequest;
    }

    public CompletableFuture<Object> $invokeAsync(RapidContext rapidContext, DubboRequest dubboRequest) {
        //	内部封装，dubbo调用请都要填充请求上下文
        fillRpcContext(rapidContext);
        //	创建泛化调用对象, 并进行缓存
        GenericService genericService = newGenericServiceForReg(dubboRequest.getRegistriesStr(),
        		dubboRequest.getInterfaceClass(),
                dubboRequest.getTimeout(),
                dubboRequest.getVersion());

        try {
        	//	执行泛化调用请求
        	CompletableFuture<Object> completableFuture = genericService.$invokeAsync(dubboRequest.getMethodName(), 
        			dubboRequest.getParameterTypes(),
            		dubboRequest.getArgs());
        	
             return completableFuture;
        } catch (Exception e) {
            throw new DubboConnectException(e, rapidContext.getUniqueId(),
            		rapidContext.getOriginRequest().getPath(),
                    dubboRequest.getInterfaceClass(),
                    dubboRequest.getMethodName(),
                    ResponseCode.DUBBO_REQUEST_ERROR);
        }
    }
    
    private void fillRpcContext(RapidContext rapidContext) {
        //  dubbo 调用负载均衡所需参数
        RpcContext.getContext().set(DUBBO_TRANSFER_CONTEXT, rapidContext);

        //  dubbo 附加信息传递
        if (rapidContext.getAttribute(AttributeKey.DUBBO_ATTACHMENT) != null) {
            RpcContext.getContext().getAttachments().putAll(rapidContext.getAttribute(AttributeKey.DUBBO_ATTACHMENT));
        }
    }

    public static Map<String, String> getDubboAttachment(RapidContext rapidContext) {
        Map<String, String> attachment = rapidContext.getAttribute(AttributeKey.DUBBO_ATTACHMENT);
        if (attachment == null) {
            attachment = new HashMap<>();
            rapidContext.putAttribute(AttributeKey.DUBBO_ATTACHMENT, attachment);
        }
        return attachment;
    }

    public void destroyAll() {
        cache.invalidateAll();
    }

    private GenericService newGenericServiceForReg(String registriesStr, 
    		String interfaceClass, 
    		int timeout,
    		String version) {
    	
        String key = registriesStr + ":" + interfaceClass + ":" + version;
        GenericService genericService = cache.get(key, s -> {
        	//	默认RegistryConfig
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(registriesStr);
            registryConfig.setCheck(false);
            registryConfig.setTimeout(20000);
            if (registriesStr.indexOf("://") < 0) {
                registryConfig.setProtocol("zookeeper");
            }
            return newGenericService(Arrays.asList(registryConfig), interfaceClass, timeout, version);
        });
        return genericService;
    }

    private GenericService newGenericService(List<RegistryConfig> registries, 
    		String interfaceClass, 
    		int timeout,
    		String version) {
    	
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT;
        }
        RapidConfig rapidConfig = RapidConfigLoader.getRapidConfig();
        int dubboConnections = rapidConfig.getDubboConnections();
        
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<GenericService>();
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistries(registries);
        referenceConfig.setTimeout(timeout);
        referenceConfig.setGeneric("true");
        referenceConfig.setInterface(interfaceClass);
        referenceConfig.setAsync(true);
        referenceConfig.setCheck(false);
        referenceConfig.setLoadbalance(DubboLoadBalance.NAME);
        
        referenceConfig.setParameters(new HashMap<>());
        referenceConfig.getParameters().put(DISPATCHER_KEY, "direct");
        referenceConfig.getParameters().put(SHARE_CONNECTIONS_KEY, String.valueOf(dubboConnections));
        if (StringUtils.isNotEmpty(version)) {
            referenceConfig.setVersion(version);
        }
        return referenceConfigCache.getCache().get(referenceConfig);
    }

}
