package rabbit.flt.plugins.mybatis;

import rabbit.flt.common.trace.TraceData;

public class MybatisTraceContext {

    private static final MybatisTraceContext inst = new MybatisTraceContext();

    private ThreadLocal<TraceData> traceDataContext = new ThreadLocal<>();

    private MybatisTraceContext() {}

    public static void setTraceData(TraceData traceData) {
        inst.traceDataContext.set(traceData);
    }

    public static TraceData getTraceData() {
        return inst.traceDataContext.get();
    }

    public static void remove() {
        inst.traceDataContext.remove();
    }
}
