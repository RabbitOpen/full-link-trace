package rabbit.flt.plugins.mybatis;

import rabbit.flt.common.trace.TraceData;

public class MybatisTraceContext {

    private static ThreadLocal<TraceData> traceDataContext = new ThreadLocal<>();

    public static void setTraceData(TraceData traceData) {
        traceDataContext.set(traceData);
    }

    public static TraceData getTraceData() {
        return traceDataContext.get();
    }

    public static void remove() {
        traceDataContext.remove();
    }
}
