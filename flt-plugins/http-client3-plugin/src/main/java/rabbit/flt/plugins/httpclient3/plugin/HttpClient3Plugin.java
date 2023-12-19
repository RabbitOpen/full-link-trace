package rabbit.flt.plugins.httpclient3.plugin;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Headers;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.exception.FltException;
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
        if (null != args[1] && isTraceOpened()) {
            MethodStackInfo stackInfo = TraceContext.getStackInfo(method);
            TraceData traceData = stackInfo.getTraceData();
            HttpMethod httpMethod = (HttpMethod) args[1];
            httpMethod.setRequestHeader(Headers.TRACE_ID, TraceContext.getTraceId());
            httpMethod.setRequestHeader(Headers.SPAN_ID, traceData.getSpanId());
            AgentConfig config = AbstractConfigFactory.getConfig();
            if (null != config) {
                httpMethod.setRequestHeader(Headers.SOURCE_APP, config.getApplicationCode());
            }
        }
        return argsReturn;
    }

    @Override
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) {
        super.fillTraceData(traceData, objectEnhanced, method, args, result);
        if (null != args[1] && isTraceOpened()) {
            traceData.setMessageType(MessageType.HTTP.name());
            traceData.setNodeName("HTTP_CLIENT3");
            HttpMethod httpMethod = (HttpMethod) args[1];
            try {
                traceData.setNodeDesc(httpMethod.getURI().toString());
            } catch (URIException e) {
                throw new FltException(e);
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
            if (200 != httpMethod.getStatusCode()) {
                try {
                    response.setBody(truncate(httpMethod.getResponseBodyAsString()));
                } catch (Exception e) {
                    throw new FltException(e);
                }
            }
            response.setStatusCode(httpMethod.getStatusCode());
            traceData.setHttpResponse(response);
        }
    }

}
