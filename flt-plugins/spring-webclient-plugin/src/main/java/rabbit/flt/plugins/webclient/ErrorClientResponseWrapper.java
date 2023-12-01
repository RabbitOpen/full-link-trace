package rabbit.flt.plugins.webclient;

import org.springframework.web.reactive.function.client.ClientResponse;
import rabbit.flt.common.trace.TraceData;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 异常响应包装
 */
public class ErrorClientResponseWrapper implements InvocationHandler {

    private TraceData traceData;

    private ClientResponse clientResponse;

    private static final ThreadLocal<TraceData> errorContext = new ThreadLocal<>();

    public ErrorClientResponseWrapper(TraceData traceData, ClientResponse clientResponse) {
        this.traceData = traceData;
        this.clientResponse = clientResponse;
    }

    /**
     * 代理对象
     * @param clientResponse
     * @param traceData
     * @return
     */
    public static ClientResponse proxy(ClientResponse clientResponse, TraceData traceData) {
        return (ClientResponse) Proxy.newProxyInstance(ErrorClientResponseWrapper.class.getClassLoader(), new Class[] {ClientResponse.class},
                new ErrorClientResponseWrapper(traceData, clientResponse));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            errorContext.set(traceData);
            return method.invoke(clientResponse, args);
        } finally {
            errorContext.remove();
        }
    }

    public static TraceData getErrorTraceData() {
        return errorContext.get();
    }
}
