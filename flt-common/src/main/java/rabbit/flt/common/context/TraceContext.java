package rabbit.flt.common.context;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.trace.MethodStackInfo;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.common.utils.UUIDUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * trace context
 */
public class TraceContext {

    private TraceContext() {}

    /**
     * trace id context
     */
    private static final ThreadLocal<String> traceIdContext = new ThreadLocal<>();

    /**
     * span id context
     */
    private static final ThreadLocal<String> spanIdContext = new ThreadLocal<>();

    /**
     * 记录method压栈情况
     */
    private static final ThreadLocal<Map<Object, MethodStackInfo>> methodStackContext = new ThreadLocal<>();

    /**
     * 记录span id下子节点个数
     */
    private static final ThreadLocal<Map<String, AtomicLong>> spanIdCounter = new ThreadLocal<>();

    /**
     * 链路节点计数器
     */
    private static final ThreadLocal<AtomicLong> nodeCounter = new ThreadLocal<>();

    /**
     * trace 状态
     */
    private static final ThreadLocal<Object> traceOpenStatusContext = new ThreadLocal<>();

    /**
     * spring web / web flux支持类，用来传递trace data给controller插件
     */
    private static final ThreadLocal<TraceData> webTraceDataContext = new ThreadLocal<>();

    /**
     * 异常标记
     */
    private static final ThreadLocal<Boolean> errorContext = new ThreadLocal<>();

    /**
     * 压栈
     * @param method
     * @return
     */
    public static MethodStackInfo pushStack(Object method) {
        return pushStack(method, TraceContext::getOrCreateTraceId, () -> calcCurrentSpanId(method));
    }

    /**
     * 压栈
     * @param method
     * @param traceIdSupplier
     * @param spanIdSupplier
     * @return
     */
    public static MethodStackInfo pushStack(Object method, Supplier<String> traceIdSupplier, Supplier<String> spanIdSupplier) {
        if (null == methodStackContext.get()) {
            methodStackContext.set(new ConcurrentHashMap<>());
        }
        MethodStackInfo stackInfo = methodStackContext.get().computeIfAbsent(method, m -> {
            incrementNode();
            MethodStackInfo info = new MethodStackInfo();
            TraceData traceData = new TraceData();
            traceData.setTraceId(traceIdSupplier.get());
            traceData.setSpanId(spanIdSupplier.get());
            if (method instanceof Method) {
                Method clzMethod = (Method) method;
                traceData.setNodeName(clzMethod.getName());
                traceData.setNodeDesc(clzMethod.getDeclaringClass().getName() + "." + clzMethod.getName());
            }
            traceData.setRequestTime(info.getRequestTime());
            traceData.setServerIp(Metrics.getHostIp());
            traceData.setStatus(TraceData.Status.OK);
            info.setTraceData(traceData);
            return info;
        });
        stackInfo.pushStack();
        openTrace(method);
        if (StringUtils.isEmpty(getTraceId()) || StringUtils.isEmpty(spanIdContext.get())) {
            TraceData traceData = stackInfo.getTraceData();
            setTraceId(traceData.getTraceId());
            String spanId = traceData.getSpanId();
            initRootSpanId(spanId.indexOf('-') == -1 ? "0" : spanId.substring(0, spanId.lastIndexOf('-')));
        }
        return stackInfo;
    }

    public static MethodStackInfo getStackInfo(Method method) {
        if (null == methodStackContext.get()) {
            return null;
        }
        return methodStackContext.get().get(method);
    }

    /**
     * 出栈
     * @param method
     * @return
     */
    public static MethodStackInfo popStack(Object method) {
        Map<Object, MethodStackInfo> map = methodStackContext.get();
        if (null == map) {
            return null;
        }
        MethodStackInfo stackInfo = map.get(method);
        stackInfo.popStack();
        if (stackInfo.isPopped()) {
            TraceData traceData = stackInfo.getTraceData();
            if (isTraceOpenedBy(method)) {
                clearContext();
            } else {
                map.remove(method);
                int endIndex = traceData.getSpanId().lastIndexOf('-');
                if (-1 != endIndex) {
                    initRootSpanId(traceData.getSpanId().substring(0, endIndex));
                }
            }
            traceData.setCost(System.currentTimeMillis() - stackInfo.getRequestTime());
        }
        return stackInfo;
    }

    /**
     * 判断是否已经打开trace
     * @return
     */
    public static boolean isTraceOpened() {
        return null != traceOpenStatusContext.get();
    }

    public static void openTrace(Object target) {
        if (isTraceOpened()) {
            return;
        }
        traceOpenStatusContext.set(target);
    }

    private static void incrementNode() {
        AtomicLong counter = nodeCounter.get();
        if (null == counter) {
            nodeCounter.set(new AtomicLong(1));
        } else {
            counter.incrementAndGet();
        }
    }

    /**
     * 计算并返回当前节点的span id
     * @param target
     * @return
     */
    public static String calcCurrentSpanId(Object target) {
        String spanId = spanIdContext.get();
        if (null == spanId) {
            spanId = initRootSpanId();
        } else {
            if (isTraceOpenedBy(target)) {
                return spanId;
            }
            spanId = spanId.concat("-").concat(getChildSpanIdIndex(spanId).toString());
            initRootSpanId(spanId);
        }
        return spanId;
    }

    private static Long getChildSpanIdIndex(String spanId) {
        return getSpanIdChildCounter(spanId).getAndAdd(1L);
    }

    /**
     * 获取span id 的counter
     * @param spanId
     * @return
     */
    public static AtomicLong getSpanIdChildCounter(String spanId) {
        Map<String, AtomicLong> map = spanIdCounter.get();
        if (null == map) {
            spanIdCounter.set(new ConcurrentHashMap<>());
            map = spanIdCounter.get();
        }
        return map.computeIfAbsent(spanId, k -> new AtomicLong(0));
    }

    /**
     * 设置counter
     * @param spanId
     * @param counter
     */
    public static void setSpanIdChildCounter(String spanId, AtomicLong counter) {
        Map<String, AtomicLong> map = spanIdCounter.get();
        if (null == map) {
            spanIdCounter.set(new ConcurrentHashMap<>());
            map = spanIdCounter.get();
        }
        map.put(spanId, counter);
    }

    /**
     * 判断是不是当前对象开启的
     * @param target
     * @return
     */
    public static boolean isTraceOpenedBy(Object target) {
        return target.equals(traceOpenStatusContext.get());
    }

    /**
     * 清理context
     */
    public static void clearContext() {
        traceIdContext.remove();
        spanIdContext.remove();
        spanIdCounter.remove();
        methodStackContext.remove();
        traceOpenStatusContext.remove();
        webTraceDataContext.remove();
        errorContext.remove();
        nodeCounter.remove();
    }

    public static long getCurrentNodeCount() {
        if (null == nodeCounter.get()) {
            return 0L;
        }
        return nodeCounter.get().longValue();
    }

    public static void flagError() {
        errorContext.set(true);
    }

    /**
     * 识别当前节点是不是异常节点
     *
     * @return
     */
    public static boolean isErrorNode() {
        return null != errorContext.get();
    }

    /**
     * 清除异常标记
     */
    public static void clearError() {
        errorContext.remove();
    }

    /**
     * 设值
     *
     * @param traceData
     */
    public static void setWebTraceDataContextData(TraceData traceData) {
        webTraceDataContext.set(traceData);
    }

    public static TraceData getWebTraceDataContextData() {
        return webTraceDataContext.get();
    }

    public static void removeWebTraceContextData() {
        webTraceDataContext.remove();
    }

    public static String initRootSpanId() {
        return initRootSpanId("0");
    }

    /**
     * 初始化span id，并返回结果
     *
     * @param spanId
     * @return
     */
    public static String initRootSpanId(String spanId) {
        spanIdContext.set(spanId);
        return spanIdContext.get();
    }

    public static String getRootSpanId() {
        return spanIdContext.get();
    }

    /**
     * 获取或者创建trace id
     *
     * @return
     */
    public static String getOrCreateTraceId() {
        if (null == traceIdContext.get()) {
            traceIdContext.set(StringUtils.toString(UUIDUtils.uuid()));
        }
        return traceIdContext.get();
    }

    /**
     * 获取已经存在的trace id
     *
     * @return
     */
    public static String getTraceId() {
        return traceIdContext.get();
    }

    public static void setTraceId(String traceId) {
        traceIdContext.set(traceId);
    }
}
