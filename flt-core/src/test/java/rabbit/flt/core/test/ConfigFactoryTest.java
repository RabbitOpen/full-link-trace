package rabbit.flt.core.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.exception.AgentException;
import rabbit.flt.core.factory.DefaultConfigFactory;

@RunWith(JUnit4.class)
public class ConfigFactoryTest {

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
}
