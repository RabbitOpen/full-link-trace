package rabbit.flt.common;


/**
 * metrics数据处理器
 */
public interface MetricsDataHandler extends Closeable {

    /**
     * 处理数据
     *
     * @param data
     * @return
     */
    boolean handle(Metrics data);

    /**
     * 优先级，值越小优先级越高
     * @return
     */
    default int getPriority() {
        return Integer.MAX_VALUE;
    }

    /**
     * 是否允许上报
     * @param type
     * @return
     */
    default boolean isMetricsEnabled(Class<? extends Metrics> type) {
        return true;
    }
}
