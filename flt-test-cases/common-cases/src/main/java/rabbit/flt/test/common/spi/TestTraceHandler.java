package rabbit.flt.test.common.spi;

import rabbit.flt.common.TraceDataHandler;
import rabbit.flt.common.trace.TraceData;

import java.util.List;

public class TestTraceHandler implements TraceDataHandler {

//    private static Logger logger = LoggerFactory.getLogger(TestTraceHandler.class);

    private static TaskHandler discardDataHandler = traceData -> {};

    private static TaskHandler traceDataHandler = null;

    public static void setDiscardDataHandler(TaskHandler discardDataHandler) {
        TestTraceHandler.discardDataHandler = discardDataHandler;
    }

    @Override
    public void process(List<TraceData> dataList) {
        for (TraceData traceData : dataList) {
            if (null != traceDataHandler) {
                traceDataHandler.process(traceData);
            }
        }
    }

    @Override
    public void discard(List<TraceData> dataList) {
        for (TraceData traceData : dataList) {
            if (null != discardDataHandler) {
                discardDataHandler.process(traceData);
            }
        }
    }

    @Override
    public int getPriority() {
        return 1000;
    }
}
