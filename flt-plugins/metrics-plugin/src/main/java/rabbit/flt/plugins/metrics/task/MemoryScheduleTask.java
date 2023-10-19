package rabbit.flt.plugins.metrics.task;

import com.sun.management.OperatingSystemMXBean;
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

    private final long interval = 15 * 1000L;

    public MemoryScheduleTask() {
        nextFireTime = System.currentTimeMillis() / interval * interval + interval;
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
        metrics.setMaxHeapMemory(mxBean.getHeapMemoryUsage().getMax() / M);
        metrics.setUsedHeapMemory(mxBean.getHeapMemoryUsage().getUsed() / M);
        metrics.setSamplingTime(nextFireTime);
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        metrics.setMaxSystemMemory(osBean.getTotalPhysicalMemorySize() / M);
        metrics.setUsedSystemMemory((osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / M);
        nextFireTime = nextFireTime + interval;
        return metrics;
    }
}
