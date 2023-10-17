package rabbit.flt.plugins.common.plugin;

import rabbit.flt.plugins.common.Plugin;

import java.lang.reflect.Method;

/**
 * 支持插件
 */
public abstract class SupportPlugin extends Plugin {

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        return args;
    }

    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        // do nothing
    }

    @Override
    public void onException(Object objectEnhanced, Method method, Object[] args, Throwable t) {
        // do nothing
    }

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        // do nothing
        return result;
    }
}
