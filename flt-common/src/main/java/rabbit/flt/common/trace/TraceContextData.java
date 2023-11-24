package rabbit.flt.common.trace;

import java.util.concurrent.atomic.AtomicLong;

public class TraceContextData {

    private String traceId;

    private String rootSpanId;

    private AtomicLong spanIdCounter;

    private long stackCounter;

    private TraceData webTraceDataContext;

    public TraceContextData(String traceId, String rootSpanId, AtomicLong spanIdCounter, TraceData webTraceDataContext) {
        this.traceId = traceId;
        this.rootSpanId = rootSpanId;
        this.spanIdCounter = spanIdCounter;
        this.webTraceDataContext = webTraceDataContext;
        this.stackCounter = 0l;
    }

    public TraceContextData(TraceContextData context) {
        this(context.getTraceId(), context.getRootSpanId(),
                context.getSpanIdCounter(), context.getWebTraceDataContext());
    }

    public long pushStack() {
        this.stackCounter++;
        return this.stackCounter;
    }

    public long popStack() {
        this.stackCounter--;
        return this.stackCounter;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getRootSpanId() {
        return rootSpanId;
    }

    public AtomicLong getSpanIdCounter() {
        return spanIdCounter;
    }

    public TraceData getWebTraceDataContext() {
        return webTraceDataContext;
    }

}
