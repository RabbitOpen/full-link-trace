package rabbit.flt.plugins.webflux.plugin;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpResponse;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;

public class ServerHttpResponsePlugin extends SupportPlugin {

    /**
     * setComplete后发送trace数据
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     */
    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        if (!(objectEnhanced instanceof TraceContextHolder)) {
            return;
        }
        TraceContextHolder holder = (TraceContextHolder) objectEnhanced;
        TraceData traceData = (TraceData) holder.getTraceContextData();
        if (null == traceData) {
            return;
        }
        traceData.setCost(System.currentTimeMillis() - traceData.getRequestTime());
        AbstractServerHttpResponse response = (AbstractServerHttpResponse) objectEnhanced;
        HttpResponse responseInfo = new HttpResponse();
        if (TraceData.Status.ERR == traceData.getStatus()) {
            responseInfo.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } else {
            responseInfo.setStatusCode(response.getRawStatusCode());
        }
        response.getHeaders().forEach((name, value) -> responseInfo.addHeader(name, truncate(value.toString())));
        traceData.setHttpResponse(responseInfo);
        super.handleTraceData(traceData);
    }

    @Override
    protected void handleTraceData(TraceData traceData) {
        // do nothing
    }
}
