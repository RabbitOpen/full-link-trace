package rabbit.flt.plugins.metrics.task;

import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.metrics.GcMetrics;

public class GcScheduleTask extends ScheduleTask<GcMetrics> {

    @Override
    public boolean isPrepared(AgentConfig config) {
        return false;
    }

    @Override
    public boolean isMetricsEnabled(AgentConfig config) {
        return config.isGcMetricsEnabled();
    }

    @Override
    public GcMetrics getMetrics() {
        return null;
    }
}
