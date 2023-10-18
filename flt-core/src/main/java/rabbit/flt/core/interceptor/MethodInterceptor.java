package rabbit.flt.core.interceptor;

import net.bytebuddy.implementation.bind.annotation.*;
import rabbit.flt.core.AbstractMethodInterceptor;
import rabbit.flt.core.callback.MethodCallback;

import java.lang.reflect.Method;

/**
 * 函数拦截器
 */
public class MethodInterceptor extends AbstractMethodInterceptor {

    public MethodInterceptor(String pluginClassName) {
        super(pluginClassName);
    }

    /**
     * 拦截成员方法
     * @param objThis
     * @param method
     * @param args
     * @param callback
     * @return
     */
    @RuntimeType
    public Object interceptMemberMethod(@This Object objThis, @Origin Method method,
                                        @AllArguments Object[] args,
                                        @Morph MethodCallback callback) {
        return intercept(method, args, objThis, params -> callback.call(params));
    }

    /**
     * 拦截静态方法
     * @param method
     * @param args
     * @param callback
     * @return
     */
    @RuntimeType
    public Object interceptStaticMethod(@Origin Method method, @AllArguments Object[] args,
                                        @Morph MethodCallback callback) {
        return intercept(method, args, null, params -> callback.call(params));
    }
}
