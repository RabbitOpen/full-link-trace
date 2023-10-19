package rabbit.flt.test.common.spi;

import rabbit.flt.common.trace.TraceData;

public interface TaskHandler {

    void process(TraceData data);
}
