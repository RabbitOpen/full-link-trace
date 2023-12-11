package rabbit.flt.plugins.springmvc.plugin;

import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;

/**
 * 提供了 controller advice 才进入该逻辑
 * spring mvc 模式下拦截response body
 */
public class SpringMvcReturnValuePlugin extends SupportPlugin {

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        TraceData traceData = SpringMethodAdapterPlugin.getTraceContext();
        if (null != traceData) {
            traceData.getHttpResponse().setBody(truncate(StringUtils.toString(args[0])));
            SpringMethodAdapterPlugin.clearContext();
            traceData.setCost(System.currentTimeMillis() - traceData.getRequestTime());
            super.handleTraceData(traceData);
        }
        return args;
    }
}
