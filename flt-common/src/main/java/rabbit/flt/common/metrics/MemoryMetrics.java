package rabbit.flt.common.metrics;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsType;

public class MemoryMetrics extends Metrics {

    /**
     * 最大内存，单位M
     */
    private long maxHeapMemory;

    /**
     * 已用内存，单位M
     */
    private long usedHeapMemory;

    /**
     * 最大内存，单位M
     */
    private long maxSystemMemory;

    /**
     * 已用内存，单位M
     */
    private long usedSystemMemory;

    /**
     * 采样时间
     */
    private long samplingTime;

    @Override
    public String getMetricsType() {
        return MetricsType.MEMORY.name();
    }

    public long getMaxHeapMemory() {
        return maxHeapMemory;
    }

    public void setMaxHeapMemory(long maxHeapMemory) {
        this.maxHeapMemory = maxHeapMemory;
    }

    public long getUsedHeapMemory() {
        return usedHeapMemory;
    }

    public void setUsedHeapMemory(long usedHeapMemory) {
        this.usedHeapMemory = usedHeapMemory;
    }

    public long getMaxSystemMemory() {
        return maxSystemMemory;
    }

    public void setMaxSystemMemory(long maxSystemMemory) {
        this.maxSystemMemory = maxSystemMemory;
    }

    public long getUsedSystemMemory() {
        return usedSystemMemory;
    }

    public void setUsedSystemMemory(long usedSystemMemory) {
        this.usedSystemMemory = usedSystemMemory;
    }

    public long getSamplingTime() {
        return samplingTime;
    }

    public void setSamplingTime(long samplingTime) {
        this.samplingTime = samplingTime;
    }
}
