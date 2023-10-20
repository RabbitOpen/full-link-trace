package rabbit.flt.common;

import java.lang.reflect.ParameterizedType;

public abstract class ScheduleTask<T extends Metrics> {

    protected Class<T> metricsTypeClz;

    // 1M
    protected final static long million = 1024L * 1024;

    public ScheduleTask() {
        Class<?> clz = getClass();
        while (true) {
            if (ScheduleTask.class == clz.getSuperclass()) {
                break;
            }
            clz = clz.getSuperclass();
        }
        ParameterizedType parameterizedType = (ParameterizedType) clz.getGenericSuperclass();
        metricsTypeClz = (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * 任务是否已经就绪
     * @param config
     * @return
     */
    public abstract boolean isPrepared(AgentConfig config);

    /**
     * 是否允许上报
     * @param config
     * @return
     */
    public abstract boolean isMetricsEnabled(AgentConfig config);

    /**
     * 获取需要上报的metrics
     * @return
     */
    public abstract T getMetrics();

    /**
     * 处理指标数据
     * @param realHandler
     * @param metrics
     */
    public void handle(MetricsDataHandler realHandler, Metrics metrics) {
        realHandler.handle(metrics);
    }

    /**
     * 获取数据类型
     * @return
     */
    public Class<T> getMetricsType() {
        return metricsTypeClz;
    }
}
