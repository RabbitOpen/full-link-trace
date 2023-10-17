package rabbit.flt.common.metrics;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsType;
import rabbit.flt.common.metrics.info.SpaceInfo;

import java.util.ArrayList;
import java.util.List;

public class DiskSpaceMetrics extends Metrics {

    private List<SpaceInfo> spaceInfoList = new ArrayList<>();

    /**
     * 采样时间
     */
    private long samplingTime;

    @Override
    public String getMetricsType() {
        return MetricsType.DISK_SPACE.name();
    }

    public List<SpaceInfo> getSpaceInfoList() {
        return spaceInfoList;
    }

    public void setSpaceInfoList(List<SpaceInfo> spaceInfoList) {
        this.spaceInfoList = spaceInfoList;
    }

    public long getSamplingTime() {
        return samplingTime;
    }

    public void setSamplingTime(long samplingTime) {
        this.samplingTime = samplingTime;
    }
}
