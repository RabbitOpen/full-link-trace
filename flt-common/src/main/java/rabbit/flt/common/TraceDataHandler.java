package rabbit.flt.common;

import rabbit.flt.common.trace.TraceData;

import java.util.List;

/**
 * trace 信息收集器
 */
public interface TraceDataHandler  {

    /**
     * 处理trace数据
     *
     * @param dataList
     */
    void process(List<TraceData> dataList);

    /**
     * 丢弃数据
     * @param dataList
     */
    default void discard(List<TraceData> dataList) {

    }

    /**
     * 值越小优先级越高
     * @return
     */
    default int getPriority() {
        return Integer.MAX_VALUE;
    }
}
