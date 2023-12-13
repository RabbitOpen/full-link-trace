package rabbit.flt.plugins.webclient.plugin;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import rabbit.flt.common.Headers;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.MessageType;
import rabbit.flt.common.trace.MethodStackInfo;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpRequest;
import rabbit.flt.common.trace.io.HttpResponse;
import rabbit.flt.common.utils.CollectionUtils;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;
import rabbit.flt.plugins.webclient.ErrorClientResponseWrapper;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

public class WebClientExchangePlugin extends PerformancePlugin {

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        if (!isTraceOpened()) {
            // 没开trace do noting
            return args;
        }
        ClientRequest clientRequest = (ClientRequest) args[0];
        HttpHeaders httpHeaders = clientRequest.headers();
        TraceContext.pushStack(method, () -> {
            String traceId = getValueFromHeader(httpHeaders, Headers.TRACE_ID);
            if (StringUtils.isEmpty(traceId)) {
                return TraceContext.getOrCreateTraceId();
            }
            return traceId;
        }, () -> {
            String spanId = getValueFromHeader(httpHeaders, Headers.SPAN_ID);
            if (StringUtils.isEmpty(spanId)) {
                return TraceContext.calcCurrentSpanId(method);
            }
            return spanId;
        });
        // 设置trace data的请求入参信息
        setRequestInfo(method, clientRequest, httpHeaders);
        return args;
    }

    /**
     * 设置trace data的请求入参信息
     *
     * @param method
     * @param clientRequest
     * @param httpHeaders
     */
    private void setRequestInfo(Method method, ClientRequest clientRequest, HttpHeaders httpHeaders) {
        HttpRequest httpRequest = new HttpRequest();
        httpHeaders.forEach((name, value) -> {
            if (!value.isEmpty()) {
                httpRequest.addHeader(name, truncate(value.toString()));
            }
        });
        httpRequest.setRequestUri(StringUtils.toString(clientRequest.url()));
        TraceContext.getStackInfo(method).getTraceData().setHttpRequest(httpRequest);
    }

    private String getValueFromHeader(HttpHeaders httpHeaders, String key) {
        List<String> values = httpHeaders.get(key);
        if (!CollectionUtils.isEmpty(values)) {
            return values.get(0);
        }
        return null;
    }

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        Mono<ClientResponse> mono = (Mono<ClientResponse>) super.after(objectEnhanced, method, args, result);
        TraceData traceData = getTraceData(TraceContext.getStackInfo(method));
        if (null == traceData) {
            return mono;
        }
        return mono.map(response -> {
            // 获取response以后设置数据
            ClientResponse.Headers defaultHeaders = response.headers();
            HttpHeaders httpHeaders = defaultHeaders.asHttpHeaders();
            HttpResponse httpResponse = new HttpResponse();
            httpHeaders.forEach((k, v) -> {
                if (!v.isEmpty()) {
                    httpResponse.addHeader(k, truncate(v.toString()));
                }
            });
            httpResponse.setStatusCode(response.statusCode().value());
            traceData.setHttpResponse(httpResponse);
            traceData.setCost(System.currentTimeMillis() - traceData.getRequestTime());
            traceData.setNodeName("WebClient");
            traceData.setMessageType(MessageType.WEBCLIENT.name());
            ClientRequest clientRequest = (ClientRequest) args[0];
            String uri = StringUtils.toString(clientRequest.url());
            traceData.setNodeDesc(uri);
            if (OK == response.statusCode()) {
                // 发送数据
                super.handleTraceData(traceData);
                return response;
            }
            return ErrorClientResponseWrapper.proxy(response, traceData);
        }).onErrorMap(e -> {
            // 服务方不在线，网络不可达等错误
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setStatusCode(500);
            httpResponse.setBody(truncate(e.getMessage()));
            traceData.setHttpResponse(httpResponse);
            traceData.setCost(System.currentTimeMillis() - traceData.getRequestTime());
            traceData.setNodeName("WebClient");
            traceData.setMessageType(MessageType.WEBCLIENT.name());
            ClientRequest clientRequest = (ClientRequest) args[0];
            String uri = StringUtils.toString(clientRequest.url());
            traceData.setNodeDesc(uri);
            super.handleTraceData(traceData);
            return e;
        });
    }

    private TraceData getTraceData(MethodStackInfo stackInfo) {
        if (null == stackInfo) {
            return null;
        }
        return stackInfo.getTraceData();
    }

    /**
     * 不发送trace， 在doFinally中发送
     *
     * @param traceData
     */
    @Override
    protected final void handleTraceData(TraceData traceData) {
        // do nothing
    }
}
