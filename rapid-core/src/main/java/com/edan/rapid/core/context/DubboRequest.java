package com.edan.rapid.core.context;

import lombok.Getter;
import lombok.Setter;

/**
 * <B>主类名称：</B>DubboRequest<BR>
 * <B>概要说明：</B>DubboRequest<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午8:22:35
 */
@Getter
@Setter
public class DubboRequest {

    //  dubbo服务的注册地址
    private String registriesStr;
    //  dubbo服务的接口名称
    private String interfaceClass;
    //  dubbo服务的方法名
    private String methodName;
    // 	dubbo服务的方法参数签名
    private String[] parameterTypes;
    // 	调用的参数内容
    private Object[] args;
    //	调用的超时时间
    private int timeout;
    // 	调用的版本号
    private String version;


}
