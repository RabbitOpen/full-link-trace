package rabbit.flt.plugins.reactor.plugin;

import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.exception.FltException;
import rabbit.flt.common.trace.TraceContextData;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class ReactorStaticSupportPlugin extends SupportPlugin {

    @Override
    public Object[] before(Object target, Method method, Object[] args) {
        if (isTraceOpened()) {
            enhanceFunction(args);
        }
        return super.before(target, method, args);
    }

    /**
     * 增强 defer / fromCallable / fromRunnable 函数参数的逻辑
     *
     * @param args
     */
    private void enhanceFunction(Object[] args) {
        TraceContextData data = new TraceContextData(TraceContext.getTraceId(),
                TraceContext.getRootSpanId(),
                TraceContext.getSpanIdChildCounter(TraceContext.getRootSpanId()),
                TraceContext.getWebTraceDataContextData());
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Runnable) {
                args[i] = getRunnable(data, (Runnable) arg);
            }
            if (arg instanceof Callable) {
                args[i] = getCallable(data, (Callable) arg);
            }
            if (arg instanceof Supplier) {
                args[i] = getSupplier(data, (Supplier) arg);
            }
        }
    }

    private Supplier getSupplier(TraceContextData data, Supplier arg) {
        return () -> tryExecuteWithTrace(data, arg::get);
    }

    private Callable getCallable(TraceContextData data, Callable arg) {
        return () -> tryExecuteWithTrace(data, arg::call);
    }

    private Runnable getRunnable(TraceContextData data, Runnable arg) {
        return () -> tryExecuteWithTrace(data, () -> {
            arg.run();
            return null;
        });
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
            throw new FltException(e);
        } finally {
            if (open) {
                TraceContext.clearContext();
            }
        }
    }
}
