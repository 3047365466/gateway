package com.edan.rapid.common.exception;

import com.edan.rapid.common.enums.ResponseCode;
import lombok.Getter;

/**
 * <B>主类名称：</B>DubboConnectException<BR>
 * <B>概要说明：</B>DubboConnectException<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午8:23:21
 */
public class DubboConnectException extends RapidConnectException {
	
    private static final long serialVersionUID = -5658789202509033456L;

    @Getter
    private final String interfaceName;
    @Getter
    private final String methodName;

    public DubboConnectException(String uniqueId, String requestUrl, String interfaceName, String methodName) {
        super(uniqueId, requestUrl);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
    }

    public DubboConnectException(Throwable cause, String uniqueId, String requestUrl, 
    		String interfaceName, String methodName, ResponseCode code) {
        super(cause, uniqueId, requestUrl, code);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
    }
    
}
