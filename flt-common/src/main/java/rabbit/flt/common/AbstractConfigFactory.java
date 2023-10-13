package rabbit.flt.common;

import java.io.InputStream;

/**
 * 抽象配置工厂
 */
public abstract class AbstractConfigFactory {

    public static final String CLASS_PATH_PREFIX = "classpath:";

    private static AbstractConfigFactory factory = null;

    // 配置文件名
    private static String agentConfigFile;

    private static ConfigFactoryLoader factoryLoader;

    /**
     * 获取配置工厂
     * @return
     */
    public static AbstractConfigFactory getFactory() {
        if (null == factory && null != getFactoryLoader()) {
            factory = getFactoryLoader().loadFactory();
        }
        return factory;
    }

    public static ConfigFactoryLoader getFactoryLoader() {
        return factoryLoader;
    }

    /**
     * 初始化
     */
    public abstract void doInitialize();

    /**
     * 初始化
     */
    public abstract void doInitialize(InputStream stream);

    public static void setFactoryLoader(ConfigFactoryLoader factoryLoader) {
        AbstractConfigFactory.factoryLoader = factoryLoader;
    }
}
