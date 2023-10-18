package rabbit.flt.plugins.reactor.plugin;

import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceContextData;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;

/**
 * subscribe subscribeWith增强
 */
public class ReactorSupportPlugin extends SupportPlugin {

    /**
     * 订阅前写入trace context
     * @param objectEnhanced
     * @param method
     * @param args
     * @return
     */
    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        TraceContextData holderData = getHolderData((TraceContextHolder) objectEnhanced);
        if (null == holderData) {
            return super.before(objectEnhanced, method, args);
        }
        holderData.pushStack();
        if (!isTraceOpened()) {
            TraceContext.openTrace(objectEnhanced);
            TraceContext.setTraceId(holderData.getTraceId());
            TraceContext.initRootSpanId(holderData.getRootSpanId());
            TraceContext.setSpanIdChildCounter(holderData.getRootSpanId(), holderData.getSpanIdCounter());
            TraceContext.setWebTraceDataContextData(holderData.getWebTraceDataContext());
        }
        return super.before(objectEnhanced, method, args);
    }

    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        TraceContextData holderData = getHolderData((TraceContextHolder) objectEnhanced);
        if (null == holderData) {
            return;
        }
        if (0 == holderData.popStack()) {
            if (TraceContext.isTraceOpenedBy(objectEnhanced)) {
                TraceContext.clearContext();
            }
        }
    }

    private TraceContextData getHolderData(TraceContextHolder holder) {
        return (TraceContextData) holder.getTraceContextData();
    }
}
