package rabbit.flt.rpc.client;

import rabbit.flt.rpc.common.rpc.RpcRequest;

/**
 * rpc请求拦截器，允许在请求前篡改数据
 */
public interface RpcRequestInterceptor {

    /**
     * 请求前置逻辑
     * @param request
     */
    default void before(RpcRequest request) {
    }

    /**
     * 值越小，越先执行
     * @return
     */
    default Integer getPriority() {
        return Integer.MIN_VALUE;
    }
}
