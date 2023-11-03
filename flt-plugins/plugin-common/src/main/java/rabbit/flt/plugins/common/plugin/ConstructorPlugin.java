package rabbit.flt.plugins.common.plugin;

import rabbit.flt.plugins.common.AbstractPlugin;

import java.lang.reflect.Method;

public abstract class ConstructorPlugin extends AbstractPlugin {

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
}
