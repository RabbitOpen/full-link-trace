package rabbit.flt.plugins.webflux.plugin;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponse;
import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpResponse;
import rabbit.flt.plugins.common.plugin.SupportPlugin;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class ServerHttpResponsePlugin extends SupportPlugin {

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        if ("writeWith".equals(method.getName()) && (args[0] instanceof Mono)) {
            // 非200请求记录response内容
            ServerHttpResponse response = (ServerHttpResponse) objectEnhanced;
            args[0] = ((Mono<DataBuffer>) args[0]).map(buffer -> {
                if (objectEnhanced instanceof TraceContextHolder) {
                    TraceContextHolder holder = (TraceContextHolder) objectEnhanced;
                    TraceData traceData = (TraceData) holder.getTraceContextData();
                    if (null != traceData && HttpStatus.OK != response.getStatusCode()) {
                        traceData.setHttpResponse(new HttpResponse());
                        byte[] bytes = new byte[buffer.readableByteCount()];
                        buffer.read(bytes);
                        traceData.getHttpResponse().setBody(truncate(new String(bytes, Charset.forName("UTF8"))));
                        DataBufferUtils.release(buffer);
                        return response.bufferFactory().wrap(bytes);
                    }
                }
                return buffer;
            });
        }
        return args;
    }

    /**
     * setComplete后发送trace数据
     *
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     */
    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        if (!(objectEnhanced instanceof TraceContextHolder) || !"setComplete".equals(method.getName())) {
            return;
        }
        TraceContextHolder holder = (TraceContextHolder) objectEnhanced;
        TraceData traceData = (TraceData) holder.getTraceContextData();
        if (null == traceData) {
            return;
        }
        traceData.setCost(System.currentTimeMillis() - traceData.getRequestTime());
        AbstractServerHttpResponse response = (AbstractServerHttpResponse) objectEnhanced;
        HttpResponse responseInfo = getHttpResponse(traceData);
        if (TraceData.Status.ERR == traceData.getStatus()) {
            responseInfo.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } else {
            responseInfo.setStatusCode(response.getRawStatusCode());
        }
        response.getHeaders().forEach((name, value) -> responseInfo.addHeader(name, truncate(value.toString())));
        traceData.setHttpResponse(responseInfo);
        super.handleTraceData(traceData);
    }

    private HttpResponse getHttpResponse(TraceData traceData) {
        return null != traceData.getHttpResponse() ? traceData.getHttpResponse() : new HttpResponse();
    }

}
