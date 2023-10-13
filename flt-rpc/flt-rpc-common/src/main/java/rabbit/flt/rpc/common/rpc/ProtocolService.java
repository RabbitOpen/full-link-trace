package rabbit.flt.rpc.common.rpc;

import rabbit.flt.common.Metrics;
import rabbit.flt.rpc.common.ServerNode;

import java.util.List;

public interface ProtocolService {

    /**
     * 获取服务器节点信息
     * @return
     */
    List<ServerNode> getServerNodes();

    /**
     * 是否允许上报指定指标数据
     * @param applicationCode
     * @param type
     * @return
     */
    boolean isMetricsEnabled(String applicationCode, Class<? extends Metrics> type);
}
