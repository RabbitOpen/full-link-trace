package rabbit.flt.core.factory;

import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.utils.ResourceUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * 配置工厂
 */
public class DefaultConfigFactory extends AbstractConfigFactory {

    private ReentrantLock lock = new ReentrantLock();

    private AgentConfig config;

    @Override
    public void doInitialize() {
        InputStream stream = null;
        try {
            lock.lock();
            if (null != config) {
                return;
            }
            String configFile = AbstractConfigFactory.getAgentConfigFile();
            if (configFile.toLowerCase().startsWith(CLASS_PATH_PREFIX)) {
                String filePath = configFile.substring(CLASS_PATH_PREFIX.length());
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
            } else {
                stream = new FileInputStream(configFile);
            }
            if (null == stream) {
                throw new AgentException("[" + configFile + "] is not exist");
            }
            doInitialize(stream);
        } catch (Exception e) {
            throw new AgentException(e);
        } finally {
            ResourceUtil.close(stream);
            lock.unlock();
        }
    }

    @Override
    protected AgentConfig getAgentConfig() {
        return config;
    }

    @Override
    public void doInitialize(InputStream stream) {
        try {
            Properties properties = new Properties();
            properties.load(stream);
            Map<String, Object> map = new HashMap<>();
            for (Object o : properties.keySet()) {
                map.put(o.toString().substring("agent.".length()), properties.get(o));
            }
            Map<Class, Function> functionMap = getFunctionMap();
            config = new AgentConfig();
            for (Field field : AgentConfig.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (map.containsKey(field.getName())) {
                    field.set(config, functionMap.get(field.getType()).apply(map.get(field.getName())));
                }
            }
            config.doValidation();
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
}
