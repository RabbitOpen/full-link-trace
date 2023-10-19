package rabbit.flt.plugins.httpclient4.plugin;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Key;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.MessageType;
import rabbit.flt.common.trace.MethodStackInfo;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpRequest;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;

import java.lang.reflect.Method;

public class HttpClient4Plugin extends PerformancePlugin {

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
                HttpMessage httpMessage = (HttpMessage) args[1];
                httpMessage.setHeader(Key.traceIdHeaderName, TraceContext.getTraceId());
                httpMessage.setHeader(Key.spanIdHeaderName, traceData.getSpanId());
                AgentConfig config = AbstractConfigFactory.getConfig();
                if (null != config) {
                    httpMessage.setHeader(Key.sourceAppHeaderName, config.getApplicationCode());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return argsReturn;
    }

    private boolean shouldEnhance(Object[] args) {
        return isHttpClient4(args) && isTraceOpened();
    }

    private boolean isHttpClient4(Object[] args) {
        return 3 == args.length && null != args[1] && args[1] instanceof HttpMessage;
    }

    @Override
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) throws Exception {
        super.fillTraceData(traceData, objectEnhanced, method, args, result);
        if (shouldEnhance(args)) {
            traceData.setMessageType(MessageType.HTTP_CLIENT4.name());
            traceData.setNodeName(MessageType.HTTP_CLIENT4.name());
            HttpRequestBase httpRequest = (HttpRequestBase) args[1];
            traceData.setNodeDesc(httpRequest.getURI().toString());
            HttpRequest request = new HttpRequest();
            for (Header header : httpRequest.getAllHeaders()) {
                request.addHeader(header.getName(), truncate(header.getValue()));
            }
            traceData.setInput(request);
            HttpResponse httpResponse = (HttpResponse) result;
            if (null != httpResponse) {
                rabbit.flt.common.trace.io.HttpResponse response = new rabbit.flt.common.trace.io.HttpResponse();
                for (Header header : httpResponse.getAllHeaders()) {
                    response.addHeader(header.getName(), truncate(header.getValue()));
                }
                response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
                traceData.setOutput(response);
            }
        }
    }
}
