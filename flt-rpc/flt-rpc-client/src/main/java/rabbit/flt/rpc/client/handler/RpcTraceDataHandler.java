package rabbit.flt.rpc.client.handler;

import rabbit.flt.common.TraceDataHandler;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.rpc.client.DataHandler;
import rabbit.flt.rpc.common.rpc.DataService;

import java.util.List;

public class RpcTraceDataHandler extends DataHandler implements TraceDataHandler {

    @Override
    public void process(List<TraceData> dataList) {
        try {
            DataService dataService = getDataService();
            if (null == dataService) {
                return;
            }
            dataService.handleTraceData(dataList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        releaseAllConnections();
    }
}
