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
    void before(RpcRequest request);
}
