package rabbit.flt.rpc.client;

import rabbit.flt.rpc.common.rpc.RpcRequest;

/**
 * 客户端
 */
public interface Client {

    /**
     * 发送rpc请求
     *
     * @param request
     * @param <T>
     * @return
     */
    <T> T doRequest(RpcRequest request);

    /**
     * 连接服务端
     */
    void doConnect();
}
