package rabbit.flt.plugins.metrics.task;

import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsDataHandler;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.metrics.EnvironmentMetrics;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.common.utils.VersionUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class EnvironmentScheduleTask extends ScheduleTask<EnvironmentMetrics> {

    private boolean scheduled = false;



    @Override
    public boolean isPrepared(AgentConfig config) {
        return !scheduled;
    }

    @Override
    public boolean isMetricsEnabled(AgentConfig config) {
        return config.isEnvMetricsEnabled();
    }

    @Override
    public EnvironmentMetrics getMetrics() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        EnvironmentMetrics metrics = new EnvironmentMetrics();
        metrics.setAgentVersion(VersionUtil.getVersion());
        metrics.setStartArgs(StringUtils.toString(runtimeMXBean.getInputArguments()));
        metrics.setProcessName(runtimeMXBean.getName());
        metrics.setJdkVersion(runtimeMXBean.getSpecVersion());
        metrics.setOnlineTime(runtimeMXBean.getStartTime());
        return metrics;
    }

    @Override
    public void handle(MetricsDataHandler realHandler, Metrics metrics) {
        scheduled = realHandler.handle(metrics);
    }
}
