package rabbit.flt.core.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.utils.VersionUtils;
import rabbit.flt.core.factory.DefaultConfigFactory;
import rabbit.flt.core.loader.DefaultPluginClassLoader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@RunWith(JUnit4.class)
public class FltCoreTest {

    @Test
    public void configFactoryTest() {
        AbstractConfigFactory.setAgentConfigFile("classpath:/agent.properties");
        DefaultConfigFactory factory = new DefaultConfigFactory();
        AbstractConfigFactory.setFactoryLoader(() -> factory);
        TestCase.assertNull(AbstractConfigFactory.getConfig());
        try {
            factory.initialize();
            throw new RuntimeException("");
        } catch (AgentException e) {

        }
        AbstractConfigFactory.setAgentConfigFile("classpath:agent.properties");
        factory.initialize();
        TestCase.assertNotNull(AbstractConfigFactory.getConfig());
        factory.initialize();

    }

    @Test
    public void classLoaderTest() throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        String baseDir = VersionUtils.getProperty("flt.properties", "baseDir");
        String jar = baseDir + "/../flt-plugins/logback-support-plugin/target/logback-support-plugin-"+ VersionUtils.getVersion()+".jar";
        DefaultPluginClassLoader classLoader = new DefaultPluginClassLoader() {
            @Override
            protected String getAgentJarFilePath() {
                return jar;
            }
        };
        Class<?> clz = classLoader.loadClassByName("rabbit.flt.plugins.logback.plugin.LineNumberPlugin");
        TestCase.assertNotNull(clz);
    }
}
