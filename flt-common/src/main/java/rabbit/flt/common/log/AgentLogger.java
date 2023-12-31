package rabbit.flt.common.log;


import rabbit.flt.common.utils.ReflectUtils;
import rabbit.flt.common.utils.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class AgentLogger implements InvocationHandler {

    private Object slf4jLogger;

    // logger的名字
    private String loggerName;

    /**
     * 一级key：日志方法名（方法重载导致有很多同名方法）
     * 二级key: 函数方法别名（同名方法，参数不同，别名不同）
     */
    private Map<String, Map<String, Method>> methodCache = new ConcurrentHashMap<>();

    public AgentLogger(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (null == slf4jLogger) {
            try {
                Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(getSlf4jLoggerClassName());
                slf4jLogger = clz.getDeclaredMethod("getLogger", String.class).invoke(null, loggerName);
            } catch (Exception e) {
                // 没有日志对象则直接控制台输出
                printWithConsole(args);
                return null;
            }
        }
        return methodCache.computeIfAbsent(method.getName(), name -> new ConcurrentHashMap<>())
                .computeIfAbsent(getMethodAlias(method.getParameterTypes()), name ->
                        ReflectUtils.loadMethod(slf4jLogger.getClass(), method.getName(), method.getParameterTypes()))
                .invoke(slf4jLogger, args);
    }

    private String getMethodAlias(Class<?>[] types) {
        if (null == types || 0 == types.length) {
            return "_$NO_PARA_LOG_METHOD";
        }
        return String.join("-", Arrays.asList(types).stream().map(Class::getSimpleName).collect(Collectors.toList()));
    }

    private String getSlf4jLoggerClassName() {
        return String.join(".", "org", "slf4j", "LoggerFactory");
    }

    /**
     * 控制台输出
     *
     * @param args
     */
    private void printWithConsole(Object[] args) {
        if (Objects.isNull(args) || 0 == args.length) {
            return;
        }
        if (1 == args.length) {
            logWithConsole(StringUtils.toString(args[0]));
            return;
        }
        String result = StringUtils.toString(args[0]);
        for (int i = 1; i < args.length; i++) {
            result = appendSimpleParameter(StringUtils.toString(args[i]), result);
        }
        logWithConsole(result);
    }

    /**
     * 整合简单参数
     *
     * @param arg
     * @param result
     * @return
     */
    private String appendSimpleParameter(Object arg, String result) {
        String errorMsg = StringUtils.toString(arg);
        if (arg instanceof Exception) {
            return result.concat(", ").concat(errorMsg);
        } else {
            return result.replaceFirst("\\{\\}", Matcher.quoteReplacement(errorMsg));
        }
    }

    /**
     * 通过控制台输出
     *
     * @param result
     */
    private void logWithConsole(String result) {
        System.out.println(result);
    }
}
