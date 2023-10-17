package rabbit.flt.common;

public interface TraceContextHolder {

    Object getTraceContextData();

    void setTraceContextData(Object traceContextData);
}
