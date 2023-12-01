package rabbit.flt.plugins.webclient.plugin;

import org.springframework.core.io.ByteArrayResource;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.SupportPlugin;
import rabbit.flt.plugins.webclient.ErrorClientResponseWrapper;
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
        if (null != traceData && result instanceof Mono) {
            Mono mono = (Mono) result;
            return mono.map(resp -> {
                String body = truncate(StringUtils.toString(resp));
                if (resp instanceof byte[]) {
                    body = truncate(new String((byte[]) resp, Charset.forName("UTF8")));
                } else if (resp instanceof ByteArrayResource) {
                    body = truncate(new String(((ByteArrayResource) resp).getByteArray(), Charset.forName("UTF8")));
                }
                traceData.getHttpResponse().setBody(body);
                super.handleTraceData(traceData);
                return resp;
            });
        }
        return result;
    }
}
