package rabbit.flt.rpc.client.handler;

import rabbit.flt.common.TraceDataHandler;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.rpc.client.DataHandler;

import java.util.List;

public class RpcTraceDataHandler extends DataHandler implements TraceDataHandler {

    @Override
    public void process(List<TraceData> dataList) {
        getDataService().handleTraceData(dataList);
    }

}
