package rabbit.flt.plugins.metrics.jna.task;

import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.metrics.DiskIoMetrics;
import rabbit.flt.plugins.metrics.jna.loader.DiskIoLoader;

public class DiskIoScheduleTask extends ScheduleTask<DiskIoMetrics> {

    private Long lastSampleTime = 0L;

    private DiskIoLoader diskIoLoader;

    @Override
    public boolean isPrepared(AgentConfig config) {
        if (null == diskIoLoader) {
            try {
                diskIoLoader = new DiskIoLoader();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        long interval = config.getDiskIoSampleIntervalSeconds() * 1000L;
        long sampleTime = System.currentTimeMillis() / interval * interval;
        if (lastSampleTime != sampleTime) {
            lastSampleTime = sampleTime;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMetricsEnabled(AgentConfig config) {
        return config.isDiskIoMetricsEnabled();
    }

    @Override
    public DiskIoMetrics getMetrics() {
        if (null == diskIoLoader) {
            return null;
        }
        DiskIoMetrics metrics = diskIoLoader.load();
        metrics.setSamplingTime(lastSampleTime);
        return metrics;
    }
}
