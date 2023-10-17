package rabbit.flt.common;

import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.utils.StringUtils;

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

    public static void setAgentConfigFile(String agentConfigFile) {
        if (StringUtils.isEmpty(agentConfigFile)) {
            throw new AgentException("agent config error, config file can't be empty!");
        }
        AbstractConfigFactory.agentConfigFile = agentConfigFile;
    }

    public static String getAgentConfigFile() {
        return agentConfigFile;
    }

}
