package rabbit.flt.common.metrics;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsType;

public class EnvironmentMetrics extends Metrics {

    private String jdkVersion;

    // 启动参数
    private String startArgs;

    // 代理版本号
    private String agentVersion;

    @Override
    public String getMetricsType() {
        return MetricsType.ENVIRONMENT.name();
    }

    public String getJdkVersion() {
        return jdkVersion;
    }

    public void setJdkVersion(String jdkVersion) {
        this.jdkVersion = jdkVersion;
    }

    public String getStartArgs() {
        return startArgs;
    }

    public void setStartArgs(String startArgs) {
        this.startArgs = startArgs;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }
}
