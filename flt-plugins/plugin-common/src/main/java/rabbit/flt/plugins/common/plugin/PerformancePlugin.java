package rabbit.flt.plugins.common.plugin;

import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.plugins.common.AbstractPlugin;

import java.lang.reflect.Method;

import static rabbit.flt.common.trace.MessageType.METHOD;

/**
 * 性能插件，统计开销
 */
public class PerformancePlugin extends AbstractPlugin {

    @Override
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) {
        traceData.setMessageType(METHOD.name());
    }

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        if (isTraceOpened()) {
            if (TraceContext.getCurrentNodeCount() > 100) {
                logger.error("long trace[{}] exception! more than 100 nodes is detected", TraceContext.getTraceId());
                TraceContext.clearContext();
            }
            return super.before(objectEnhanced, method, args);
        }
        return args;
    }

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        if (isTraceOpened()) {
            return super.after(objectEnhanced, method, args, result);
        }
        return result;
    }

    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        if (isTraceOpened()) {
            super.doFinal(objectEnhanced, method, args, result);
        }
    }

    @Override
    public void onException(Object objectEnhanced, Method method, Object[] args, Throwable t) {
        if (isTraceOpened()) {
            super.onException(objectEnhanced, method, args, t);
        }
    }
}
