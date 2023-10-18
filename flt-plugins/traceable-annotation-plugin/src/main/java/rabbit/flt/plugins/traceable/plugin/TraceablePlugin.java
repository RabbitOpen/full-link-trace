package rabbit.flt.plugins.traceable.plugin;

import rabbit.flt.common.context.TraceContext;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;

import java.lang.reflect.Method;

public class TraceablePlugin extends PerformancePlugin {

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        TraceContext.openTrace(method);
        return super.before(objectEnhanced, method, args);
    }
}
