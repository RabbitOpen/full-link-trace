package rabbit.flt.common.metrics;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsType;

public class CpuMetrics extends Metrics {

    /**
     * 进程占用的cpu
     */
    private Double processCpu;

    /**
     * 系统占用的cpu
     */
    private Double systemCpu;

    /**
     * 核数
     */
    private int coreCount;

    /**
     * 采样时间
     */
    private long samplingTime;

    @Override
    public String getMetricsType() {
        return MetricsType.CPU.name();
    }

    public Double getProcessCpu() {
        return processCpu;
    }

    public void setProcessCpu(Double processCpu) {
        this.processCpu = processCpu;
    }

    public Double getSystemCpu() {
        return systemCpu;
    }

    public void setSystemCpu(Double systemCpu) {
        this.systemCpu = systemCpu;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(int coreCount) {
        this.coreCount = coreCount;
    }

    public long getSamplingTime() {
        return samplingTime;
    }

    public void setSamplingTime(long samplingTime) {
        this.samplingTime = samplingTime;
    }
}
