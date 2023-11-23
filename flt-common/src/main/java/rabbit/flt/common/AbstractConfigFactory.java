package rabbit.flt.common;

import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.utils.ReflectUtils;
import rabbit.flt.common.utils.StringUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

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
    public abstract void initialize();

    /**
     * 获取配置对象
     * @return
     */
    protected abstract AgentConfig getAgentConfig();

    /**
     * 获取配置对象
     * @return
     */
    public static AgentConfig getConfig() {
        AbstractConfigFactory factory = getFactory();
        if (null == factory) {
            throw new AgentException("no factory exception");
        }
        return factory.getAgentConfig();
    }

    /**
     * 读取配置
     */
    public final AgentConfig loadConfig(InputStream stream) {
        try {
            Properties properties = new Properties();
            properties.load(stream);
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                map.put(entry.getKey().toString().substring("agent.".length()), entry.getValue());
            }
            Map<Class, Function> functionMap = getFunctionMap();
            AgentConfig config = new AgentConfig();
            for (Field field : AgentConfig.class.getDeclaredFields()) {
                if (map.containsKey(field.getName())) {
                    Object value = functionMap.get(field.getType()).apply(map.get(field.getName()));
                    String setMethod = "set".concat(field.getName().substring(0, 1).toUpperCase().concat(field.getName().substring(1)));
                    Method declaredMethod = ReflectUtils.loadMethod(AgentConfig.class, setMethod, field.getType());
                    declaredMethod.invoke(config, value);
                }
            }
            config.doValidation();
            return config;
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    private Map<Class, Function> getFunctionMap() {
        Map<Class, Function> map = new HashMap<>();
        map.put(String.class, Object::toString);
        map.put(Boolean.class, s -> Boolean.parseBoolean(s.toString()));
        map.put(boolean.class, s -> Boolean.parseBoolean(s.toString()));
        map.put(Integer.class, s -> Integer.parseInt(s.toString()));
        map.put(int.class, s -> Integer.parseInt(s.toString()));
        map.put(Long.class, s -> Long.parseLong(s.toString()));
        map.put(long.class, s -> Long.parseLong(s.toString()));
        map.put(Double.class, s -> Double.parseDouble(s.toString()));
        map.put(double.class, s -> Double.parseDouble(s.toString()));
        return map;
    }

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
