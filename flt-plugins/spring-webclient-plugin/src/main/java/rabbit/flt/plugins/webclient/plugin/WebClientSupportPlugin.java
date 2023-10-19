package rabbit.flt.plugins.webclient.plugin;

import org.springframework.web.reactive.function.client.WebClient;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Key;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

public class WebClientSupportPlugin extends SupportPlugin {

    @Override
    public Object[] before(Object target, Method method, Object[] args) {
        WebClient.RequestBodyUriSpec spec = (WebClient.RequestBodyUriSpec) target;
        if (isTraceOpened()) {
            try {
                String rootSpanId = TraceContext.getRootSpanId();
                AtomicLong spanIdChildCounter = TraceContext.getSpanIdChildCounter(rootSpanId);
                String spanId = rootSpanId.concat("-").concat(Long.toString(spanIdChildCounter.getAndAdd(1L)));
                addWebClientTraceHeader(spec, spanId, TraceContext.getTraceId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return args;
    }

    /**
     * 添加 header
     * @param spec
     * @param spanId
     * @param traceId
     */
    private void addWebClientTraceHeader(WebClient.RequestBodyUriSpec spec, String spanId, String traceId) {
        spec.headers(httpHeaders -> {
            httpHeaders.set(Key.spanIdHeaderName, spanId);
            httpHeaders.set(Key.traceIdHeaderName, traceId);
        });
        // 添加源app
        AgentConfig config = AbstractConfigFactory.getConfig();
        if (null != config) {
            spec.headers(httpHeaders -> httpHeaders.set(Key.sourceAppHeaderName, config.getApplicationCode()));
        }
    }
}
