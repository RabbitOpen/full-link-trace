package rabbit.flt.core;

import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.utils.ReflectUtils;
import rabbit.flt.core.callback.MethodCallback;
import rabbit.flt.plugins.common.Plugin;

import java.lang.reflect.Method;

/**
 * 抽象方法拦截
 */
public abstract class AbstractMethodInterceptor implements Interceptor {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    private Plugin plugin;

    protected String pluginClassName;

    public AbstractMethodInterceptor(String pluginClassName) {
        this.pluginClassName = pluginClassName;
    }

    /**
     * 方法拦截
     *
     * @param method
     * @param args
     * @param objectEnhance
     * @param callback
     * @return
     */
    protected final Object intercept(Method method, Object[] args, Object objectEnhance, MethodCallback callback) {
        Object result = null;
        try {
            Object[] params = before(objectEnhance, method, args);
            result = after(objectEnhance, method, args, callback.call(params));
            return result;
        } catch (Exception t) {
            onException(objectEnhance, method, args, t);
            throw t;
        } finally {
            doFinal(objectEnhance, method, args, result);
        }
    }

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        return getPlugin().before(objectEnhanced, method, args);
    }

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        return getPlugin().after(objectEnhanced, method, args, result);
    }

    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        safeRun(() -> getPlugin().doFinal(objectEnhanced, method, args, result));
    }

    @Override
    public void onException(Object objectEnhanced, Method method, Object[] args, Throwable t) {
        safeRun(() -> getPlugin().onException(objectEnhanced, method, args, t));
    }

    private void safeRun(Runnable job) {
        try {
            job.run();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected Plugin getPlugin() {
        if (null != plugin) {
            return plugin;
        }
        synchronized (this) {
            if (null != plugin) {
                return plugin;
            }
            try {
                Class<?> clz = PluginClassLoader.getPluginClassLoader().loadClassByName(this.pluginClassName);
                plugin = (Plugin) ReflectUtils.newInstance(clz);
                return plugin;
            } catch (Exception e) {
                throw new AgentException(e);
            }
        }
    }
}
