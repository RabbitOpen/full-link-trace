package rabbit.flt.plugins.reactor.plugin;

import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceContextData;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ReactorExchangeSupportPlugin extends SupportPlugin {

    /**
     * 增强map 和 flatmap
     *
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
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Function) {
                // 替换map / flatmap 等转换函数逻辑
                args[i] = createExchangeFunction(target, holderData, (Function) args[i]);
            } else if (args[i] instanceof BiConsumer) {
                args[i] = createExchangeConsumer(target, holderData, (BiConsumer) args[i]);
            }
        }
        return super.before(target, method, args);
    }

    private Function createExchangeFunction(Object target, TraceContextData holderData, Function mapFunc) {
        return t -> {
            boolean noTrace = tryOpenTrace(target, holderData);
            try {
                return mapFunc.apply(t);
            } finally {
                if (noTrace && TraceContext.isTraceOpenedBy(target)) {
                    TraceContext.clearContext();
                }
            }
        };
    }

    private boolean tryOpenTrace(Object target, TraceContextData holderData) {
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
        return noTrace;
    }

    private BiConsumer createExchangeConsumer(Object target, TraceContextData holderData, BiConsumer mapFunc) {
        return (t, u) -> {
            boolean noTrace = tryOpenTrace(target, holderData);
            try {
                mapFunc.accept(t, u);
            } finally {
                if (noTrace && TraceContext.isTraceOpenedBy(target)) {
                    TraceContext.clearContext();
                }
            }
        };
    }
}
