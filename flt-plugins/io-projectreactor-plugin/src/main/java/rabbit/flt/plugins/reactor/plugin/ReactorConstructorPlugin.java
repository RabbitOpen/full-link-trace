package rabbit.flt.plugins.reactor.plugin;

import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceContextData;
import rabbit.flt.plugins.common.plugin.ConstructorPlugin;

import java.lang.reflect.Method;

public class ReactorConstructorPlugin extends ConstructorPlugin {

    /**
     * 保存trace上下文
     * @param target
     * @param method
     * @param args
     * @param result
     * @return
     */
    @Override
    public Object after(Object target, Method method, Object[] args, Object result) {
        String clzName = target.getClass().getName();
        if ("reactor.core.publisher.MonoEmpty".equals(clzName) ||
                "reactor.core.publisher.FluxEmpty".equals(clzName)) {
            // 忽略单例对象
            return result;
        }
        TraceContextHolder holder = (TraceContextHolder) target;
        if (null != holder.getTraceContextData()) {
            // 如果已经存在就直接返回
            return result;
        }
        if (TraceContext.isTraceOpened()) {
            TraceContextData data = new TraceContextData(TraceContext.getTraceId(),
                    TraceContext.getRootSpanId(),
                    TraceContext.getSpanIdChildCounter(TraceContext.getRootSpanId()),
                    TraceContext.getWebTraceDataContextData());
            holder.setTraceContextData(data);
            return result;
        }
        if (null != args) {
            return result;
        }
        for (Object arg : args) {
            if (!(arg instanceof TraceContextHolder)) {
                continue;
            }
            TraceContextHolder tch = (TraceContextHolder) arg;
            TraceContextData data = (TraceContextData) tch.getTraceContextData();
            if (null != data) {
                holder.setTraceContextData(new TraceContextData(data));
            }
        }
        return result;
    }
}
