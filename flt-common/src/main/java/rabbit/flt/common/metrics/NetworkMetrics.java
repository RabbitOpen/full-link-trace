package rabbit.flt.common.metrics;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsType;
import rabbit.flt.common.metrics.info.NetFlowInfo;

import java.util.ArrayList;
import java.util.List;

public class NetworkMetrics extends Metrics {

    private List<NetFlowInfo> flowInfoList = new ArrayList<>();

    /**
     * 采样时间
     */
    private long samplingTime;


    @Override
    public String getMetricsType() {
        return MetricsType.NETWORK.name();
    }

    public List<NetFlowInfo> getFlowInfoList() {
        return flowInfoList;
    }

    public void setFlowInfoList(List<NetFlowInfo> flowInfoList) {
        this.flowInfoList = flowInfoList;
    }

    public long getSamplingTime() {
        return samplingTime;
    }

    public void setSamplingTime(long samplingTime) {
        this.samplingTime = samplingTime;
    }
}
