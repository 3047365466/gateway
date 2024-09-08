package com.edan.rapid.core.balance;

import com.edan.rapid.common.enums.LoadBalanceStrategy;

import java.util.HashMap;
import java.util.Map;


/**
 * <B>主类名称：</B>LoadBalanceFactory<BR>
 * <B>概要说明：</B>LoadBalanceFactory<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午2:59:03
 */
public class LoadBalanceFactory {

    private final Map<LoadBalanceStrategy, LoadBalance> loadBalanceMap = new HashMap<>();
    private static final LoadBalanceFactory INSTANCE = new LoadBalanceFactory();

    private LoadBalanceFactory() {
        loadBalanceMap.put(LoadBalanceStrategy.RANDOM, new RandomLoadBalance());
        loadBalanceMap.put(LoadBalanceStrategy.ROUND_ROBIN, new RoundRobinLoadBalance());
    }

    public static LoadBalance getLoadBalance(LoadBalanceStrategy loadBalance) {
        return INSTANCE.loadBalanceMap.get(loadBalance);
    }

}
