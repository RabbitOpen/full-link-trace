package rabbit.flt.plugins.httpclient4.plugin;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Headers;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.exception.FltException;
import rabbit.flt.common.trace.MessageType;
import rabbit.flt.common.trace.MethodStackInfo;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpRequest;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;

import java.lang.reflect.Method;

public class HttpClient4Plugin extends PerformancePlugin {

    /**
     * 增强请求
     *
     * @param target
     * @param method
     * @param args
     * @return
     */
    @Override
    public Object[] before(Object target, Method method, Object[] args) {
        Object[] argsReturn = super.before(target, method, args);
        if (isTraceOpened() && null != args[1]) {
            MethodStackInfo stackInfo = TraceContext.getStackInfo(method);
            TraceData traceData = stackInfo.getTraceData();
            HttpMessage httpMessage = (HttpMessage) args[1];
            httpMessage.setHeader(Headers.TRACE_ID, TraceContext.getTraceId());
            httpMessage.setHeader(Headers.SPAN_ID, traceData.getSpanId());
            AgentConfig config = AbstractConfigFactory.getConfig();
            if (null != config) {
                httpMessage.setHeader(Headers.SOURCE_APP, config.getApplicationCode());
            }
        }
        return argsReturn;
    }

    @Override
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) {
        super.fillTraceData(traceData, objectEnhanced, method, args, result);
        if (null != args[1] && isTraceOpened()) {
            traceData.setMessageType(MessageType.HTTP.name());
            traceData.setNodeName("HTTP_CLIENT4");
            HttpRequestBase httpRequest = (HttpRequestBase) args[1];
            traceData.setNodeDesc(httpRequest.getURI().toString());
            HttpRequest request = new HttpRequest();
            for (Header header : httpRequest.getAllHeaders()) {
                request.addHeader(header.getName(), truncate(header.getValue()));
            }
            traceData.setHttpRequest(request);
            traceData.setHttpResponse(getHttpResponse((HttpResponse) result));
        }
    }

    private rabbit.flt.common.trace.io.HttpResponse getHttpResponse(HttpResponse httpResponse) {
        if (null == httpResponse) {
            return null;
        }
        rabbit.flt.common.trace.io.HttpResponse response = new rabbit.flt.common.trace.io.HttpResponse();
        for (Header header : httpResponse.getAllHeaders()) {
            response.addHeader(header.getName(), truncate(header.getValue()));
        }
        response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        if (200 != response.getStatusCode()) {
            try {
                String body = EntityUtils.toString(httpResponse.getEntity());
                // entity不能重复读取，这里获取后需要填充一个回去
                httpResponse.setEntity(new StringEntity(body));
                response.setBody(truncate(body));
            } catch (Exception e) {
                throw new FltException(e);
            }
        }
        return response;
    }
}
