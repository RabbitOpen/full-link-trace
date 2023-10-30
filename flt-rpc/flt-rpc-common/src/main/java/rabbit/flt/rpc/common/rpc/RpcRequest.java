package rabbit.flt.rpc.common.rpc;

import rabbit.flt.rpc.common.Protocol;
import rabbit.flt.rpc.common.Request;
import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.exception.AuthenticationException;
import rabbit.flt.rpc.common.exception.ChannelClosedException;
import rabbit.flt.rpc.common.exception.RpcTimeoutException;
import rabbit.flt.rpc.common.exception.UnRegisteredHandlerException;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static rabbit.flt.rpc.common.ResponseCode.*;

public class RpcRequest extends Protocol<Request> {

    /**
     * 请求计数器
     */
    private transient int counter = 0;

    // 响应
    private RpcResponse response;

    // 最大重试次数
    private transient int maxRetryTimes;

    private transient ResponseHolder<RpcResponse> responseHolder;

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
     *
     * @param timeoutSeconds
     * @param finalCallback
     * @return
     */
    public final Object getResponse(int timeoutSeconds, Runnable finalCallback) {
        if ("reactor.core.publisher.Mono".equals(getReturnTypeName())) {
            return Mono.create(responseHolder).map(resp -> {
                try {
                    return readResponseData(resp);
                } finally {
                    finalCallback.run();
                }
            });
        }
        try {
            if (responseInTime(timeoutSeconds)) {
                return readResponseData(getResponse());
            }
            throw new RpcTimeoutException(getRequest().getInterfaceClz().getName().concat(".").concat(getRequest().getMethodName()),
                    getRequestId(), timeoutSeconds);
        } finally {
            finalCallback.run();
        }
    }

    private <T> T readResponseData(RpcResponse response) {
        if (response.isSuccess()) {
            return (T) response.getData();
        }
        if (UN_AUTHENTICATED == response.getCode()) {
            throw new AuthenticationException(response.getMsg());
        }
        if (UN_REGISTERED_HANDLER == response.getCode()) {
            throw new UnRegisteredHandlerException(response.getMsg());
        }
        if (CHANNEL_CLOSED == response.getCode()) {
            throw new ChannelClosedException(response.getMsg());
        }
        throw new RpcException(response.getMsg());
    }

    private String getReturnTypeName() {
        Request request = getRequest();
        try {
            return request.getInterfaceClz().getDeclaredMethod(request.getMethodName(),
                    request.getParameterTypes()).getReturnType().getName();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    private boolean responseInTime(int timeoutSeconds) {
        try {
            return getSemaphore().tryAcquire(1, timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void setRequest(Request request) {
        responseHolder = new ResponseHolder();
        super.setRequest(request);
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
        if (null != responseHolder) {
            responseHolder.setResponse(response);
        }
    }
}
