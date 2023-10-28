package rabbit.flt.core.factory;

import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.utils.ResourceUtils;

import java.io.FileInputStream;
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
            if (configFile.toLowerCase().startsWith(CLASS_PATH_PREFIX)) {
                String filePath = configFile.substring(CLASS_PATH_PREFIX.length());
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
            } else {
                stream = new FileInputStream(configFile);
            }
            if (null == stream) {
                throw new AgentException("[" + configFile + "] is not exist");
            }
            config = loadConfig(stream);
        } catch (Exception e) {
            throw new AgentException(e);
        } finally {
            ResourceUtils.close(stream);
            lock.unlock();
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
