package rabbit.flt.plugins.metrics.task;

import com.sun.management.OperatingSystemMXBean;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.metrics.MemoryMetrics;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryScheduleTask extends ScheduleTask<MemoryMetrics> {

    /**
     * 下次触发时间
     */
    protected long nextFireTime;

    public MemoryScheduleTask() {
        long interval = getReportIntervalMils();
        nextFireTime = System.currentTimeMillis() / interval * interval + interval;
    }

    private long getReportIntervalMils() {
        int interval = 15;
        AgentConfig config = AbstractConfigFactory.getConfig();
        if (null != config) {
            interval = config.getMemoryReportIntervalSeconds();
        }
        return interval * 1000L;
    }

    @Override
    public boolean isPrepared(AgentConfig config) {
        return System.currentTimeMillis() >= this.nextFireTime;
    }

    @Override
    public boolean isMetricsEnabled(AgentConfig config) {
        return config.isMemoryMetricsEnabled();
    }

    @Override
    public MemoryMetrics getMetrics() {
        MemoryMetrics metrics = new MemoryMetrics();
        MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
        metrics.setMaxHeapMemory(mxBean.getHeapMemoryUsage().getMax() / MILLION);
        metrics.setUsedHeapMemory(mxBean.getHeapMemoryUsage().getUsed() / MILLION);
        metrics.setSamplingTime(nextFireTime);
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        metrics.setMaxSystemMemory(osBean.getTotalPhysicalMemorySize() / MILLION);
        metrics.setUsedSystemMemory((osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / MILLION);
        nextFireTime = nextFireTime + getReportIntervalMils();
        return metrics;
    }
}
