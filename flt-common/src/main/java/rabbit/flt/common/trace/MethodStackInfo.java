package rabbit.flt.common.trace;

/**
 * 栈信息
 */
public class MethodStackInfo {

    /**
     * 压栈次数
     */
    private long pushStackTimes = 0L;

    private long requestTime;

    private TraceData traceData;

    public MethodStackInfo() {
        requestTime = System.currentTimeMillis();
    }

    public long getRequestTime() {
        return requestTime;
    }

    public TraceData getTraceData() {
        return traceData;
    }

    public void setTraceData(TraceData traceData) {
        this.traceData = traceData;
    }

    /**
     * 入栈
     */
    public void pushStack() {
        this.pushStackTimes++;
        this.traceData.pushStack();
    }

    /**
     * 出栈
     */
    public void popStack() {
        if (isPopped()) {
            return;
        }
        this.pushStackTimes--;
    }

    /**
     * 是否已经出栈
     * @return
     */
    public boolean isPopped() {
        return 0 == pushStackTimes;
    }
}
