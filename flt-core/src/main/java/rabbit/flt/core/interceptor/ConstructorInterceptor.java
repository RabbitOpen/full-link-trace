package rabbit.flt.core.interceptor;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import rabbit.flt.core.AbstractMethodInterceptor;

import java.lang.reflect.Method;

/**
 * 构造函数拦截器
 */
public class ConstructorInterceptor extends AbstractMethodInterceptor {

    public ConstructorInterceptor(String pluginClassName) {
        super(pluginClassName);
    }

    @RuntimeType
    public void doIntercept(@AllArguments Object[] args, @This Object objectEnhanced) {
        after(objectEnhanced, null, args, null);
    }

    @Override
    public void onException(Object objectEnhanced, Method method, Object[] args, Throwable t) {
        // ignore
    }
}
