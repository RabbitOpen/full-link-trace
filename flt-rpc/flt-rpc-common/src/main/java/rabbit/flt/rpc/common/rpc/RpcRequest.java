package rabbit.flt.rpc.common.rpc;

import rabbit.flt.rpc.common.Protocol;
import rabbit.flt.rpc.common.Request;
import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.exception.RpcTimeoutException;
import rabbit.flt.rpc.common.exception.UnAuthenticatedException;
import rabbit.flt.rpc.common.exception.UnRegisteredHandlerException;

import java.util.concurrent.TimeUnit;

import static rabbit.flt.rpc.common.ResponseCode.CHANNEL_CLOSED;
import static rabbit.flt.rpc.common.ResponseCode.UN_AUTHENTICATED;
import static rabbit.flt.rpc.common.ResponseCode.UN_REGISTERED_HANDLER;

public class RpcRequest extends Protocol<Request> {

    /**
     * 请求计数器
     */
    private transient int counter = 0;

    // 响应
    private RpcResponse response;

    // 最大重试次数
    private transient int maxRetryTimes;

    public RpcRequest() {
        super(null);
    }

    public void increase() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }

    /**
     * 读取响应信息
     * @param timeoutSeconds
     * @param <T>
     * @return
     */
    public final <T> T getResponse(int timeoutSeconds) {
        if (responseInTime(timeoutSeconds)) {
            RpcResponse response = getResponse();
            if (response.isSuccess()) {
                return (T) response.getData();
            }
            if (UN_AUTHENTICATED == response.getCode()) {
                throw new UnAuthenticatedException(response.getMsg());
            }
            if (UN_REGISTERED_HANDLER == response.getCode()) {
                throw new UnRegisteredHandlerException(response.getMsg());
            }
            if (CHANNEL_CLOSED == response.getCode()) {
                throw new UnRegisteredHandlerException(response.getMsg());
            }
            throw new RpcException(response.getMsg());
        }
        throw new RpcTimeoutException(getRequestId(), timeoutSeconds);
    }

    private boolean responseInTime(int timeoutSeconds) {
        try {
            return getSemaphore().tryAcquire(1, timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public RpcResponse getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
    }
}
