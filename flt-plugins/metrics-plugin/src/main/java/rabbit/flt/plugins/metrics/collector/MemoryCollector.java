package rabbit.flt.plugins.metrics.collector;

import com.sun.management.OperatingSystemMXBean;
import rabbit.flt.common.metrics.MemoryMetrics;
import rabbit.flt.plugins.metrics.MetricsCollector;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryCollector extends MetricsCollector<MemoryMetrics> {

    @Override
    public MemoryMetrics getMetrics() {
        MemoryMetrics metrics = new MemoryMetrics();
        MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
        metrics.setMaxHeapMemory(mxBean.getHeapMemoryUsage().getMax() / million);
        metrics.setUsedHeapMemory(mxBean.getHeapMemoryUsage().getUsed() / million);

        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        metrics.setMaxSystemMemory(osBean.getTotalPhysicalMemorySize() / million);
        metrics.setUsedSystemMemory((osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / million);
        return metrics;
    }
}
