package rabbit.flt.plugins.metrics;


import rabbit.flt.common.Metrics;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;

public abstract class MetricsCollector <T extends Metrics> {

    protected Logger logger = AgentLoggerFactory.getLogger(getClass());

    protected final long million = 1024L * 1024;

    /**
     * 收集
     * @return
     */
    public T doCollect() {
        return getMetrics();
    }

    /**
     * 生成指标
     * @return
     */
    public abstract T getMetrics();
}
