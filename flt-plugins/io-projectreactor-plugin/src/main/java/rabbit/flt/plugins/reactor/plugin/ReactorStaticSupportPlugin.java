package rabbit.flt.plugins.reactor.plugin;

import com.sun.deploy.trace.Trace;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.trace.TraceContextData;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class ReactorStaticSupportPlugin extends SupportPlugin {

    @Override
    public Object[] before(Object target, Method method, Object[] args) {
        if (isTraceOpened()) {
            TraceContextData data = new TraceContextData(TraceContext.getTraceId(),
                    TraceContext.getRootSpanId(),
                    TraceContext.getSpanIdChildCounter(TraceContext.getRootSpanId()),
                    TraceContext.getWebTraceDataContextData(), null);
            enhanceFunction(args, data);
        }
        return super.before(target, method, args);
    }

    /**
     * 增强 defer / fromCallable / fromRunnable 函数参数的逻辑
     * @param args
     * @param data
     */
    private void enhanceFunction(Object[] args, TraceContextData data) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Runnable) {
                args[i] = (Runnable) () -> tryExecuteWithTrace(data, () -> {
                    ((Runnable) arg).run();
                    return null;
                });
            }
            if (arg instanceof Callable) {
                args[i] = (Callable) () -> tryExecuteWithTrace(data, () -> ((Callable<?>) arg).call());
            }
            if (arg instanceof Supplier) {
                args[i] = (Supplier) () -> tryExecuteWithTrace(data, () -> ((Supplier<?>) arg).get());
            }
        }
    }

    private Object tryExecuteWithTrace(TraceContextData contextData, Callable task) {
        boolean open = false;
        if (!isTraceOpened()) {
            open = true;
            TraceContext.openTrace(task);
            TraceContext.setTraceId(contextData.getTraceId());
            TraceContext.initRootSpanId(contextData.getRootSpanId());
            TraceContext.setSpanIdChildCounter(contextData.getRootSpanId(), contextData.getSpanIdCounter());
            TraceContext.setWebTraceDataContextData(contextData.getWebTraceDataContext());
        }
        try {
            return task.call();
        } catch (Exception e) {
            throw new AgentException(e);
        } finally {
            if (open) {
                TraceContext.clearContext();
            }
        }
    }
}
