package rabbit.flt.plugins.logback.plugin;

import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;

public class LogbackPlugin extends SupportPlugin {

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        return args;
    }

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        String traceId = TraceContext.getTraceId();
        if (StringUtils.isEmpty(traceId)) {
            return result;
        }
        return "[".concat(traceId).concat("]-") + result;
    }
}
