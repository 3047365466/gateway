
package com.edan.rapid.core.rolling;

import lombok.Getter;

/**
 * <B>主类名称：</B>RollingNumberEvent<BR>
 * <B>概要说明：</B>环形数组事件类型类<BR>
 * @author JiFeng
 * @since 2021年12月21日 上午12:53:42
 */
public enum RollingNumberEvent {

    SUCCESS(1, 1),	//	成功
    FAILURE(1, 2),	//	失败
    REQUEST_TIMEOUT(1, 3),	// 	请求慢调用, BLOCK 
    ROUTE_TIMEOUT(1, 4)		// 	路由转发慢调用, BLOCK 
    ;	

    private final int type;

    @Getter
    private final int name;

    RollingNumberEvent(int type, int name) {
        this.type = type;
        this.name = name;
    }

    public boolean isCounter() {
        return type == 1;
    }

    public boolean isMaxUpdater() {
        return type == 2;
    }
}
