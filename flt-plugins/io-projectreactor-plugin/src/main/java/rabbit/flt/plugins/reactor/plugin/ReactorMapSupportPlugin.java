package rabbit.flt.plugins.reactor.plugin;

import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceContextData;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;
import java.util.function.Function;

public class ReactorMapSupportPlugin extends SupportPlugin {

    /**
     * 增强map 和 flatmap
     * @param target
     * @param method
     * @param args
     * @return
     */
    @Override
    public Object[] before(Object target, Method method, Object[] args) {
        TraceContextHolder holder = (TraceContextHolder) target;
        TraceContextData holderData = (TraceContextData) holder.getTraceContextData();
        if (null == holderData) {
            return super.before(target, method, args);
        }
        if (1 == args.length && args[0] instanceof Function) {
            // 替换map / flatmap
            args[0] = createMapFunction(target, holderData, (Function) args[0]);
        }
        return super.before(target, method, args);
    }

    private Function createMapFunction(Object target, TraceContextData holderData, Function mapFunc) {
        return t -> {
            boolean noTrace = false;
            if (!TraceContext.isTraceOpened()) {
                noTrace = true;
                // 传递值
                TraceContext.openTrace(target);
                TraceContext.setTraceId(holderData.getTraceId());
                TraceContext.initRootSpanId(holderData.getRootSpanId());
                TraceContext.setSpanIdChildCounter(holderData.getRootSpanId(), holderData.getSpanIdCounter());
                TraceContext.setWebTraceDataContextData(holderData.getWebTraceDataContext());
            }
            try {
                return mapFunc.apply(t);
            } finally {
                if (noTrace && TraceContext.isTraceOpenedBy(target)) {
                    TraceContext.clearContext();
                }
            }
        };
    }
}
