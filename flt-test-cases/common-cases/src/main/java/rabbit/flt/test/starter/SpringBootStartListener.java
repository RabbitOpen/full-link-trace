package rabbit.flt.test.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.core.AgentHelper;

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
                    final TraceConfiguration defaultConfig = new TraceConfiguration();
                    defaultConfig.setServers("10.20.30.40:8080");
                    defaultConfig.setApplicationCode("agent-test");
                    defaultConfig.setSecurityKey("1234567812345678");
                    initLoggerFactory();
                    AbstractConfigFactory.setFactoryLoader(() -> new AbstractConfigFactory() {
                        @Override
                        public void doInitialize() {

                        }

                        @Override
                        protected AgentConfig getAgentConfig() {
                            TraceConfiguration tc = SpringBootInitializer.getBean(TraceConfiguration.class);
                            if (null == tc) {
                                return defaultConfig;
                            }
                            return tc;
                        }

                        @Override
                        public void doInitialize(InputStream stream) {
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
