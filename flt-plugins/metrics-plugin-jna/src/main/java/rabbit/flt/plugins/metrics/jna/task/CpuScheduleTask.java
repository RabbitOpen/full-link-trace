package rabbit.flt.plugins.metrics.jna.task;

import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.metrics.CpuMetrics;
import rabbit.flt.plugins.metrics.jna.loader.CpuRateLoader;

public class CpuScheduleTask extends ScheduleTask<CpuMetrics> {

    private Long lastSampleTime = 0L;

    private CpuRateLoader cpuRateLoader;

    @Override
    public boolean isPrepared(AgentConfig config) {
        if (null == cpuRateLoader) {
            try {
                cpuRateLoader = new CpuRateLoader();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        long interval = config.getCpuSampleIntervalSeconds() * 1000L;
        long sampleTime = System.currentTimeMillis() / interval * interval;
        if (sampleTime != lastSampleTime) {
            lastSampleTime = sampleTime;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMetricsEnabled(AgentConfig config) {
        return config.isCpuMetricsEnabled();
    }

    @Override
    public CpuMetrics getMetrics() {
        if (null == cpuRateLoader) {
            return null;
        }
        CpuMetrics cpuMetrics = new CpuMetrics();
        cpuMetrics.setSystemCpu(cpuRateLoader.getSystemCpu());
        String processId = cpuMetrics.getProcessName().split("@")[0];
        cpuMetrics.setProcessCpu(cpuRateLoader.getProcessCpu(Integer.parseInt(processId)));
        cpuMetrics.setCoreCount(cpuRateLoader.getCoreCount());
        cpuMetrics.setSamplingTime(this.lastSampleTime);
        return cpuMetrics;
    }
}
