package rabbit.flt.plugins.logback.plugin;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import rabbit.flt.common.utils.CollectionUtils;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LineNumberPlugin extends SupportPlugin {

    private Map<String, String> ignoreClass = new ConcurrentHashMap<>();

    public LineNumberPlugin() {
        this.ignoreClass.put("java.lang.reflect.Method", "1");
        this.ignoreClass.put("rabbit.flt.common.log.AgentLogger", "1");
    }

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        if (!(args[0] instanceof ILoggingEvent)) {
            return result;
        }

        ILoggingEvent le = (ILoggingEvent) args[0];
        StackTraceElement[] cda = le.getCallerData();
        if (!CollectionUtils.isEmpty(cda)) {
            for (StackTraceElement stackTraceElement : cda) {
                String className = stackTraceElement.getClassName();
                if (ignoreClass.containsKey(className) || className.startsWith("com.sun.proxy.$Proxy")
                        || className.startsWith("sun.reflect.")) {
                    continue;
                }
                return Integer.toString(stackTraceElement.getLineNumber());
            }
        }
        return CallerData.NA;
    }
}
