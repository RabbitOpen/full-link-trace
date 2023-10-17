package rabbit.flt.common;

import rabbit.flt.common.trace.TraceData;

/**
 * trace 数据拦截器
 */
public interface TraceInterceptor {

    /**
     * 发送前置处理
     * @param   data
     * @return  false 丢弃数据
     */
    boolean filter(TraceData data);
}
