package rabbit.flt.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.metrics.EnvironmentMetrics;
import rabbit.flt.plugins.metrics.task.EnvironmentScheduleTask;

@RunWith(JUnit4.class)
public class MetricsTest {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    @Test
    public void loadVersionTest() {
        EnvironmentMetrics metrics = new EnvironmentScheduleTask().getMetrics();
        logger.info("agent version: {}", metrics.getAgentVersion());
        TestCase.assertNotNull(metrics.getAgentVersion());
    }
}
