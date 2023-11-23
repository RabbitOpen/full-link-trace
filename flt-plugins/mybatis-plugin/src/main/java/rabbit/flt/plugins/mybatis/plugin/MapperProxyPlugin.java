package rabbit.flt.plugins.mybatis.plugin;

import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.MethodStackInfo;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.utils.ReflectUtils;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;
import rabbit.flt.plugins.mybatis.MybatisTraceContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

public class MapperProxyPlugin extends PerformancePlugin {

    private Field h;

    private Field mapperInterface;

    private ReentrantLock lock = new ReentrantLock();

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        super.before(objectEnhanced, method, args);
        if (isTraceOpened()) {
            MethodStackInfo methodStackInfo = TraceContext.getStackInfo(method);
            TraceData traceData = methodStackInfo.getTraceData();
            MybatisTraceContext.setTraceData(traceData);
        }
        return args;
    }

    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        MybatisTraceContext.remove();
        super.doFinal(objectEnhanced, method, args, result);
    }

    @Override
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) {
        // dao 接口方法对象
        // org.apache.ibatis.binding.MapperProxy.invoke方法
        Method interfaceMethod = (Method) args[1];
        traceData.setNodeName(interfaceMethod.getName());
        Object mapperProxy = args[0];
        if (null == h || null == mapperInterface) {
            initFields(mapperProxy);
        }
        Object realMapperProxy = ReflectUtils.getValue(mapperProxy, h);
        Class<?> clz = ReflectUtils.getValue(realMapperProxy, mapperInterface);
        traceData.setNodeDesc(StringUtils.toString(clz.getName()) + ".".concat(interfaceMethod.getName()));
    }

    private void initFields(Object mapperProxy) {
        try {
            lock.lock();
            if (null != h) {
                return;
            }
            h = getField(mapperProxy.getClass(), "h");
            if (null == h) {
                return;
            }
            h.setAccessible(true);
            mapperInterface = getField(h.get(mapperProxy).getClass(), "mapperInterface");
            if (null == mapperInterface) {
                return;
            }
            mapperInterface.setAccessible(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    private Field getField(Class<?> objClz, String name) {
        Class<?> clz = objClz;
        while (true) {
            try {
                return ReflectUtils.loadField(clz, name);
            } catch (Exception e) {
                clz = clz.getSuperclass();
            }
            if (Object.class.equals(clz)) {
                return null;
            }
        }
    }
}
