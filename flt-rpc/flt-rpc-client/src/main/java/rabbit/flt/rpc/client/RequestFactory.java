package rabbit.flt.rpc.client;

import rabbit.flt.rpc.common.Request;
import rabbit.flt.rpc.common.rpc.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 请求工厂
 */
public abstract class RequestFactory implements InvocationHandler {

    /**
     * rpc 超时时间
     */
    protected int timeoutSeconds = 30;

    /**
     * 最大重试次数
     */
    protected int maxRetryTime = 3;

    /**
     * 获取代理对象
     * @param clz
     * @param <T>
     * @return
     */
    public <T> T proxy(Class<T> clz) {
        return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        if ("toString".equals(methodName)) {
            return "proxyInterface";
        }
        if ("equals".equals(methodName)) {
            return false;
        }
        if ("hashCode".equals(methodName)) {
            return -3;
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.increaseRequestId();
        Request request = new Request();
        request.setMethodName(methodName);
        request.setInterfaceClz(method.getDeclaringClass());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        rpcRequest.setRequest(request);
        rpcRequest.setMaxRetryTimes(getMaxRetryTime());
        return getClient().doRequest(rpcRequest, getRequestTimeoutSeconds());
    }

    /**
     * 获取客户端
     * @return
     */
    protected abstract Client getClient();

    protected int getRequestTimeoutSeconds() {
        return timeoutSeconds;
    }

    protected int getMaxRetryTime() {
        return maxRetryTime;
    }
}
