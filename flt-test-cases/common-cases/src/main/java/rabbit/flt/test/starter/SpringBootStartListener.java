package rabbit.flt.test.starter;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.utils.ResourceUtils;
import rabbit.flt.core.AgentHelper;
import rabbit.flt.core.factory.DefaultConfigFactory;

import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

public class SpringBootStartListener implements ApplicationListener, Ordered {

    private static ReentrantLock lock = new ReentrantLock();

    private static boolean initialized = false;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        try {
            lock.lock();
            if (!initialized) {
                initialized = true;
                final AgentConfig defaultConfig = getAgentConfig();
                AbstractConfigFactory.setFactoryLoader(() -> new AbstractConfigFactory() {

                    @Override
                    public void initialize() {
                    }

                    @Override
                    protected AgentConfig getAgentConfig() {
                        return defaultConfig;
                    }

                });
                AgentHelper.installPlugins();
            }
        } finally {
            lock.unlock();
        }
    }

    private AgentConfig getAgentConfig() {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("agent.properties");
        try {
            return new DefaultConfigFactory().loadConfig(resource);
        } finally {
            ResourceUtils.close(resource);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
