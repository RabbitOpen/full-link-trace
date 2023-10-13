package rabbit.flt.common.log;

import java.lang.reflect.Proxy;

/**
 * 日志工厂
 */
public class AgentLoggerFactory {

    /**
     * 日志工具对象
     */
    private static LoggerFactory factory = new LoggerFactory() {
        @Override
        public Logger getLogger(String name) {
            return (Logger) Proxy.newProxyInstance(AgentLoggerFactory.class.getClassLoader(),
                    new Class[]{Logger.class}, new AgentLogger(name));
        }

        @Override
        public Logger getLogger(Class<?> clz) {
            return getLogger(clz.getName());
        }
    };

    /**
     * 获取日志对象
     * @param name
     * @return
     */
    public static Logger getLogger(String name) {
        return factory.getLogger(name);
    }

    /**
     * 获取日志对象
     * @param clz
     * @return
     */
    public static Logger getLogger(Class<?> clz) {
        return factory.getLogger(clz);
    }

    /**
     * 设置日志工厂
     * @param factory
     */
    public static void setFactory(LoggerFactory factory) {
        AgentLoggerFactory.factory = factory;
    }
}
