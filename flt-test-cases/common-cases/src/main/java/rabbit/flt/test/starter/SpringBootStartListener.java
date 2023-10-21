package rabbit.flt.test.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.utils.ResourceUtil;
import rabbit.flt.core.AgentHelper;
import rabbit.flt.core.factory.DefaultConfigFactory;

import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

public class SpringBootStartListener implements ApplicationListener, Ordered {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static ReentrantLock lock = new ReentrantLock();

    private static boolean initialized = false;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        try {
            lock.lock();
            if (!initialized) {
                initialized = true;
                try {
                    final AgentConfig defaultConfig = getAgentConfig();
                    initLoggerFactory();
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
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
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
            ResourceUtil.close(resource);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void initLoggerFactory() {
        AgentLoggerFactory.setFactory(new rabbit.flt.common.log.LoggerFactory() {
            @Override
            public rabbit.flt.common.log.Logger getLogger(String name) {
                return new LoggerProxy(LoggerFactory.getLogger(name));
            }

            @Override
            public rabbit.flt.common.log.Logger getLogger(Class<?> clz) {
                return new LoggerProxy(LoggerFactory.getLogger(clz));
            }
        });
    }
}
