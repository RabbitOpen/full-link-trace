package rabbit.flt.core;

import java.lang.reflect.Method;

public interface Interceptor {

    /**
     * 前置拦截
     * @param objectEnhanced
     * @param method
     * @param args
     * @return
     */
    default Object[] before(Object objectEnhanced, Method method, Object[] args) {
        return args;
    }

    /**
     * 后置拦截
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     * @return
     */
    Object after(Object objectEnhanced, Method method, Object[] args, Object result);

    /**
     * 兜底拦截
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     */
    void doFinal(Object objectEnhanced, Method method, Object[] args, Object result);

    /**
     * 异常拦截
     * @param objectEnhanced
     * @param method
     * @param args
     * @param t
     */
    void onException(Object objectEnhanced, Method method, Object[] args, Throwable t);
}
