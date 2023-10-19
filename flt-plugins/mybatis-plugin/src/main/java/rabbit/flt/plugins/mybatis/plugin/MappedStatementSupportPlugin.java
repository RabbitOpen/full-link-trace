package rabbit.flt.plugins.mybatis.plugin;

import org.apache.ibatis.mapping.BoundSql;
import rabbit.flt.common.trace.MessageType;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.plugins.common.plugin.SupportPlugin;
import rabbit.flt.plugins.mybatis.MybatisTraceContext;

import java.lang.reflect.Method;

public class MappedStatementSupportPlugin extends SupportPlugin {

    /**
     * 填充sql到trace data
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     */
    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        TraceData traceData = MybatisTraceContext.getTraceData();
        if (null == traceData || null == result) {
            return;
        }
        traceData.setMessageType(MessageType.MYBATIS.name());
        try {
            BoundSql boundSql = (BoundSql) result;
            traceData.setData(boundSql.getSql());
        } catch (Exception e) {
            // 版本不兼容时可能报错
            logger.error(e.getMessage());
        }
    }
}
