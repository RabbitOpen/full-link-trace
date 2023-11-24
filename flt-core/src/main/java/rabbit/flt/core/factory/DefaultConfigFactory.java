package rabbit.flt.core.factory;

import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.utils.ResourceUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 配置工厂
 */
public class DefaultConfigFactory extends AbstractConfigFactory {

    private ReentrantLock lock = new ReentrantLock();

    private AgentConfig config;

    @Override
    public void initialize() {
        InputStream stream = null;
        try {
            lock.lock();
            if (null != config) {
                return;
            }
            String configFile = AbstractConfigFactory.getAgentConfigFile();
            stream = getConfigResource(configFile);
            if (null == stream) {
                throw new AgentException("[" + configFile + "] is not exist");
            }
            this.setConfig(loadConfig(stream));
        } finally {
            ResourceUtils.close(stream);
            lock.unlock();
        }
    }

    private InputStream getConfigResource(String configFile) {
        if (configFile.toLowerCase().startsWith(CLASS_PATH_PREFIX)) {
            String filePath = configFile.substring(CLASS_PATH_PREFIX.length());
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        } else {
            try {
                return  new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
    }

    @Override
    protected AgentConfig getAgentConfig() {
        return config;
    }

    public final void setConfig(AgentConfig config) {
        this.config = config;
    }
}
