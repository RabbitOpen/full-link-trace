package rabbit.flt.rpc.client;

import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.rpc.RpcRequest;

/**
 * 客户端
 */
public interface Client {

    /**
     * 发送rpc请求
     * @param request
     * @param timeoutSeconds
     * @param <T>
     * @return
     * @throws RpcException
     */
    <T> T doRequest(RpcRequest request, int timeoutSeconds) throws RpcException;

    /**
     * 连接服务端
     */
    void doConnect();
}
