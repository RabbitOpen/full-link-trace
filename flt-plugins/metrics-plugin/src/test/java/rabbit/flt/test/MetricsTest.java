package rabbit.flt.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.metrics.EnvironmentMetrics;
import rabbit.flt.common.metrics.GcMetrics;
import rabbit.flt.common.metrics.MemoryMetrics;
import rabbit.flt.plugins.metrics.task.EnvironmentScheduleTask;
import rabbit.flt.plugins.metrics.task.GcScheduleTask;
import rabbit.flt.plugins.metrics.task.MemoryScheduleTask;

import java.io.InputStream;

@RunWith(JUnit4.class)
public class MetricsTest {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    @Test
    public void loadVersionTest() {
        EnvironmentMetrics metrics = new EnvironmentScheduleTask().getMetrics();
        logger.info("agent version: {}", metrics.getAgentVersion());
        TestCase.assertNotNull(metrics.getAgentVersion());
    }

    @Test
    public void metricsTest() {
        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setMemoryReportIntervalSeconds(10);
        AbstractConfigFactory.setFactoryLoader(() -> new AbstractConfigFactory() {
            @Override
            public void doInitialize() {

            }

            @Override
            protected AgentConfig getAgentConfig() {
                return agentConfig;
            }

            @Override
            public void doInitialize(InputStream stream) {

            }
        });

        GcMetrics gcMetrics = new GcScheduleTask().getMetrics();
        if (null != gcMetrics) {
            logger.info("gc: {}", gcMetrics.getDetail());
        }

        MemoryMetrics metrics = new MemoryScheduleTask().getMetrics();
        logger.info("maxHeapMemory: {}M, usedHeapMemory: {}M", metrics.getMaxHeapMemory(), metrics.getUsedHeapMemory());
        logger.info("maxSystemMemory: {}M, usedSystemMemory: {}M", metrics.getMaxSystemMemory(), metrics.getUsedSystemMemory());
        TestCase.assertNotNull(metrics.getSamplingTime());

        EnvironmentMetrics envMetrics = new EnvironmentScheduleTask().getMetrics();
        TestCase.assertNotNull(envMetrics.getAgentVersion());
        TestCase.assertNotNull(envMetrics.getJdkVersion());
        TestCase.assertNotNull(envMetrics.getStartArgs());


    }
}
