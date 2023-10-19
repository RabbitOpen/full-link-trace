package rabbit.flt.plugins.metrics.task;

import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsDataHandler;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.metrics.EnvironmentMetrics;
import rabbit.flt.common.utils.ResourceUtil;
import rabbit.flt.common.utils.StringUtils;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Properties;

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
        metrics.setAgentVersion(getAgentVersion());
        metrics.setStartArgs(StringUtils.toString(runtimeMXBean.getInputArguments()));
        metrics.setProcessName(runtimeMXBean.getName());
        metrics.setJdkVersion(runtimeMXBean.getSpecVersion());
        metrics.setOnlineTime(runtimeMXBean.getStartTime());
        return metrics;
    }

    private String getAgentVersion() {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("agent-version.properties");
        try {
            Properties properties = new Properties();
            properties.load(resource);
            return StringUtils.toString(properties.get("version"));
        } catch (Exception e) {
            throw new AgentException(e);
        } finally {
            ResourceUtil.close(resource);
        }
    }

    @Override
    public void handle(MetricsDataHandler realHandler, Metrics metrics) {
        scheduled = realHandler.handle(metrics);
    }
}
