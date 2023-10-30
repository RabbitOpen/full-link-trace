package rabbit.flt.plugins.webflux.plugin;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import rabbit.flt.common.Headers;
import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.MessageType;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpRequest;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;

import java.lang.reflect.Method;

public class SpringWebFluxPlugin extends PerformancePlugin {

    /**
     * 拦截http request，注入traceId和spanId等信息
     *
     * @param target
     * @param method
     * @param args
     * @return
     */
    @Override
    public Object[] before(Object target, Method method, Object[] args) {
        ServerHttpRequest request = (ServerHttpRequest) args[0];
        ServerHttpResponse response = (ServerHttpResponse) args[1];
        TraceContext.clearContext();
        TraceContext.openTrace(method);
        String spanId = request.getHeaders().getFirst(Headers.SPAN_ID);
        if (StringUtils.isEmpty(spanId)) {
            TraceContext.initRootSpanId("0");
        } else {
            TraceContext.initRootSpanId(spanId.concat("-0"));
            String traceId = request.getHeaders().getFirst(Headers.TRACE_ID);
            if (!StringUtils.isEmpty(traceId)) {
                TraceContext.setTraceId(traceId);
            }
        }
        super.before(target, method, args);
        TraceData traceData = TraceContext.getStackInfo(method).getTraceData();
        setSourceApplication(request, traceData);
        traceData.setNodeName("");
        TraceContext.setWebTraceDataContextData(traceData);
        if (response instanceof TraceContextHolder) {
            TraceContextHolder holder = (TraceContextHolder) response;
            holder.setTraceContextData(traceData);
        }
        return args;
    }

    /**
     * 设置来源app字段到trace data中
     * @param request
     * @param traceData
     */
    private void setSourceApplication(ServerHttpRequest request, TraceData traceData) {
        String sourceApp = request.getHeaders().getFirst(Headers.SOURCE_APP);
        if (!StringUtils.isEmpty(sourceApp)) {
            traceData.setSourceApplication(StringUtils.toString(sourceApp));
        }
    }

    @Override
    protected final void handleTraceData(TraceData traceData) {
        // do nothing, ServerHttpResponsePlugin会发送数据
    }

    @Override
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) {
        ServerHttpRequest request = (ServerHttpRequest) args[0];
        if (StringUtils.isEmpty(traceData.getNodeName())) {
            traceData.setNodeName(request.getPath().value());
        }
        traceData.setMessageType(MessageType.REST.name());
        setRequestData(traceData, request);
    }

    private void setRequestData(TraceData traceData, ServerHttpRequest request) {
        HttpRequest requestInfo = new HttpRequest();
        requestInfo.setContextPath(request.getPath().contextPath().value());
        requestInfo.setMethod(request.getMethod().name());
        requestInfo.setRequestUri(request.getPath().value());
        HttpHeaders headers = request.getHeaders();
        headers.forEach((name, values) -> requestInfo.addHeader(name, truncate(values.toString())));
        MultiValueMap<String, String> params = request.getQueryParams();
        params.forEach((name, values) -> requestInfo.addParameter(name, values.get(0)));
        traceData.setHttpRequest(requestInfo);
    }
}
