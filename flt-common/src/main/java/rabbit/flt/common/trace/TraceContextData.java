package rabbit.flt.common.trace;

import java.util.concurrent.atomic.AtomicLong;

public class TraceContextData {

    private String traceId;

    private String rootSpanId;

    private AtomicLong spanIdCounter;

    private long stackCounter;

    private TraceData webTraceDataContext;

    private Object owner;

    public TraceContextData(String traceId, String rootSpanId, AtomicLong spanIdCounter, TraceData webTraceDataContext, Object owner) {
        this.traceId = traceId;
        this.rootSpanId = rootSpanId;
        this.spanIdCounter = spanIdCounter;
        this.webTraceDataContext = webTraceDataContext;
        this.owner = owner;
        this.stackCounter = 0l;
    }

    public TraceContextData(TraceContextData context, Object owner) {
        this(context.getTraceId(), context.getRootSpanId(),
                context.getSpanIdCounter(), context.getWebTraceDataContext(), owner);
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

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRootSpanId() {
        return rootSpanId;
    }

    public void setRootSpanId(String rootSpanId) {
        this.rootSpanId = rootSpanId;
    }

    public AtomicLong getSpanIdCounter() {
        return spanIdCounter;
    }

    public void setSpanIdCounter(AtomicLong spanIdCounter) {
        this.spanIdCounter = spanIdCounter;
    }

    public long getStackCounter() {
        return stackCounter;
    }

    public void setStackCounter(long stackCounter) {
        this.stackCounter = stackCounter;
    }

    public TraceData getWebTraceDataContext() {
        return webTraceDataContext;
    }

    public void setWebTraceDataContext(TraceData webTraceDataContext) {
        this.webTraceDataContext = webTraceDataContext;
    }

    public Object getOwner() {
        return owner;
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }
}
