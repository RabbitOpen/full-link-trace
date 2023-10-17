package rabbit.flt.common.metrics;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsType;
import rabbit.flt.common.metrics.info.DiskIoInfo;

import java.util.ArrayList;
import java.util.List;

public class DiskIoMetrics extends Metrics {

    private List<DiskIoInfo> diskIoInfoList = new ArrayList<>();

    /**
     * 采样时间
     */
    private long samplingTime;

    @Override
    public String getMetricsType() {
        return MetricsType.DISK_IO.name();
    }

    public List<DiskIoInfo> getDiskIoInfoList() {
        return diskIoInfoList;
    }

    public void setDiskIoInfoList(List<DiskIoInfo> diskIoInfoList) {
        this.diskIoInfoList = diskIoInfoList;
    }

    public long getSamplingTime() {
        return samplingTime;
    }

    public void setSamplingTime(long samplingTime) {
        this.samplingTime = samplingTime;
    }
}
