package rabbit.flt.plugins.httpclient3.plugin;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Headers;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.trace.MessageType;
import rabbit.flt.common.trace.MethodStackInfo;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpRequest;
import rabbit.flt.common.trace.io.HttpResponse;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;

import java.lang.reflect.Method;

public class HttpClient3Plugin extends PerformancePlugin {

    /**
     * 增强请求
     * @param target
     * @param method
     * @param args
     * @return
     */
    @Override
    public Object[] before(Object target, Method method, Object[] args) {
        Object[] argsReturn = super.before(target, method, args);
        if (shouldEnhance(args)) {
            try {
                MethodStackInfo stackInfo = TraceContext.getStackInfo(method);
                TraceData traceData = stackInfo.getTraceData();
                HttpMethod httpMethod = (HttpMethod) args[1];
                httpMethod.setRequestHeader(Headers.TRACE_ID, TraceContext.getTraceId());
                httpMethod.setRequestHeader(Headers.SPAN_ID, traceData.getSpanId());
                AgentConfig config = AbstractConfigFactory.getConfig();
                if (null != config) {
                    httpMethod.setRequestHeader(Headers.SOURCE_APP, config.getApplicationCode());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return argsReturn;
    }

    private boolean shouldEnhance(Object[] args) {
        return isHttpClient3(args) && isTraceOpened();
    }

    private boolean isHttpClient3(Object[] args) {
        return 3 == args.length && null != args[1] && args[1] instanceof HttpMethod;
    }

    @Override
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) {
        super.fillTraceData(traceData, objectEnhanced, method, args, result);
        if (shouldEnhance(args)) {
            traceData.setMessageType(MessageType.HTTP_CLIENT3.name());
            traceData.setNodeName(MessageType.HTTP_CLIENT3.name());
            HttpMethod httpMethod = (HttpMethod) args[1];
            try {
                traceData.setNodeDesc(httpMethod.getURI().toString());
            } catch (URIException e) {
                throw new AgentException(e);
            }
            HttpRequest request = new HttpRequest();
            for (Header header : httpMethod.getRequestHeaders()) {
                request.addHeader(header.getName(), truncate(header.getValue()));
            }
            traceData.setHttpRequest(request);
            HttpResponse response = new HttpResponse();
            for (Header header : httpMethod.getResponseHeaders()) {
                response.addHeader(header.getName(), truncate(header.getValue()));
            }
            response.setStatusCode(httpMethod.getStatusCode());
            traceData.setHttpResponse(response);
        }
    }
}
