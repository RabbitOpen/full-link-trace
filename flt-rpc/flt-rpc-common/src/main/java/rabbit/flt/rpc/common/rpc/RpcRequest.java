package rabbit.flt.rpc.common.rpc;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
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
    @FieldSerializer.Optional("RpcRequest.counter")
    private int counter = 0;

    /**
     * 请求时间
     */
    @FieldSerializer.Optional("RpcRequest.requestTime")
    private long requestTime;

    /**
     * 超时秒数
     */
    @FieldSerializer.Optional("RpcRequest.timeoutSeconds")
    private int timeoutSeconds;

    // 响应
    private RpcResponse response;

    /**
     * 最大重试次数
     */
    @FieldSerializer.Optional("RpcRequest.maxRetryTimes")
    private int maxRetryTimes;

    @FieldSerializer.Optional("RpcRequest.responseHolder")
    private ResponseHolder<RpcResponse> responseHolder;

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
     * @param finalCallback
     * @return
     */
    public final Object getResponse(Runnable finalCallback) {
        if (isAsyncRequest()) {
            return Mono.create(responseHolder).flatMap(resp -> {
                try {
                    Object data = readResponseData(resp);
                    return null == data ? Mono.empty() : Mono.just(data);
                } finally {
                    finalCallback.run();
                }
            });
        }
        try {
            if (responseInTime()) {
                return readResponseData(getResponse());
            }
            throw new RpcTimeoutException(getRequest().getInterfaceClz().getName().concat(".").concat(getRequest().getMethodName()),
                    getRequestId(), timeoutSeconds);
        } finally {
            finalCallback.run();
        }
    }

    /**
     * 异步请求
     *
     * @return
     */
    public boolean isAsyncRequest() {
        return "reactor.core.publisher.Mono".equals(getReturnTypeName());
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
        try {
            Request request = getRequest();
            return request.getInterfaceClz().getDeclaredMethod(request.getMethodName(),
                    request.getParameterTypes()).getReturnType().getName();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    private boolean responseInTime() {
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

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
