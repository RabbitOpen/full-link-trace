package rabbit.flt.plugins.common;

import java.lang.reflect.Method;

/**
 * 插件
 */
public interface Plugin {

    /**
     * 方法前置增强
     * @param objectEnhanced
     * @param method
     * @param args
     * @return 返回入参
     * @throws Exception
     */
    Object[] before(Object objectEnhanced, Method method, Object[] args);


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
     * final增强
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     */
    void doFinal(Object objectEnhanced, Method method, Object[] args, Object result);

    /**
     * 异常兜底
     * @param objectEnhanced
     * @param method
     * @param args
     * @param t
     */
    void onException(Object objectEnhanced, Method method, Object[] args, Throwable t);
}
