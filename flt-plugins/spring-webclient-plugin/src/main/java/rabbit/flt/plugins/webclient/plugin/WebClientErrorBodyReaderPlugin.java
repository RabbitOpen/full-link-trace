package rabbit.flt.plugins.webclient.plugin;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.SupportPlugin;
import rabbit.flt.plugins.webclient.ErrorClientResponseWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

/**
 * web client请求报错时（statusCode != 200）读取错误body的插件
 */
public class WebClientErrorBodyReaderPlugin extends SupportPlugin {

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        TraceData traceData = ErrorClientResponseWrapper.getErrorTraceData();
        if (null != traceData ) {
            if (result instanceof Mono) {
                return ((Mono<Object>) result).map(resp -> doMap(traceData, resp));
            } else {
                return ((Flux<Object>) result).map(resp -> doMap(traceData, resp));
            }
        }
        return result;
    }

    private Object doMap(TraceData traceData, Object resp) {
        String body = truncate(StringUtils.toString(resp));
        if (resp instanceof byte[]) {
            body = new String((byte[]) resp, Charset.forName("UTF8"));
        } else if (resp instanceof ByteArrayResource) {
            byte[] bytes = ((ByteArrayResource) resp).getByteArray();
            body = new String(bytes, Charset.forName("UTF8"));
        } else if (resp instanceof DataBuffer) {
            DataBuffer buffer = (DataBuffer) resp;
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            body = truncate(new String(bytes));
            buffer.readPosition(0);
        }
        traceData.getHttpResponse().setBody(truncate(body));
        super.handleTraceData(traceData);
        return resp;
    }
}
