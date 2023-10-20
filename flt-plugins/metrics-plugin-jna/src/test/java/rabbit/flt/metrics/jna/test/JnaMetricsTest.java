package rabbit.flt.metrics.jna.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.metrics.DiskIoMetrics;
import rabbit.flt.common.metrics.MemoryMetrics;
import rabbit.flt.common.metrics.NetworkMetrics;
import rabbit.flt.common.metrics.info.DiskIoInfo;
import rabbit.flt.plugins.metrics.jna.loader.CpuRateLoader;
import rabbit.flt.plugins.metrics.jna.loader.DiskIoLoader;
import rabbit.flt.plugins.metrics.jna.loader.NetWorkLoader;
import rabbit.flt.plugins.metrics.jna.task.CpuScheduleTask;
import rabbit.flt.plugins.metrics.jna.task.DiskIoScheduleTask;
import rabbit.flt.plugins.metrics.jna.task.DiskSpaceScheduleTask;
import rabbit.flt.plugins.metrics.jna.task.NetWorkScheduleTask;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class JnaMetricsTest {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    @Test
    public void loaderTest() {
        CpuRateLoader cpuRateLoader = new CpuRateLoader();
        MemoryMetrics metrics = new MemoryMetrics();
        String processId = metrics.getProcessName().substring(0, metrics.getProcessName().indexOf('@'));
        logger.info("getProcessCpu: {}", cpuRateLoader.getProcessCpu(Integer.parseInt(processId)));
        logger.info("getSystemCpu: {}", cpuRateLoader.getSystemCpu());
        TestCase.assertTrue(0 != cpuRateLoader.getCoreCount());

        DiskIoLoader diskIoLoader = new DiskIoLoader();
        DiskIoMetrics load = diskIoLoader.load();
        logger.info("samplingTime: {}", load.getSamplingTime());
        for (DiskIoInfo info : load.getDiskIoInfoList()) {
            logger.info("deviceName: {}", info.getDeviceName());
        }

        NetWorkLoader netWorkLoader = new NetWorkLoader();
        NetworkMetrics wlan = netWorkLoader.load(Arrays.asList(netWorkLoader.getNetworkIFs().get(0).getName()));
        TestCase.assertTrue(0 != wlan.getFlowInfoList().size());
        logger.info("netCard: {}", wlan.getFlowInfoList().get(0).getNetCard());
    }

    @Test
    public void taskTest() {
        AgentConfig config = new AgentConfig();
        config.setCpuSampleIntervalSeconds(1);
        config.setNetworkSampleIntervalSeconds(1);
        config.setDiskIoSampleIntervalSeconds(1);
        config.setDiskSpaceSampleIntervalSeconds(1);
        config.setDiskIoMetricsEnabled(true);
        config.setCpuMetricsEnabled(true);
        config.setNetMetricsEnabled(true);
        config.setDiskMetricsEnabled(true);
        config.setDiskSpaceMetricsDirs("/");
        NetWorkLoader netWorkLoader = new NetWorkLoader();
        config.setNetMetricsCards(netWorkLoader.getNetworkIFs().get(0).getName());

        CpuScheduleTask cpuScheduleTask = new CpuScheduleTask();
        TestCase.assertTrue(cpuScheduleTask.isMetricsEnabled(config));
        TestCase.assertTrue(cpuScheduleTask.isPrepared(config));
        TestCase.assertNotNull(cpuScheduleTask.getMetrics());

        NetWorkScheduleTask netWorkScheduleTask = new NetWorkScheduleTask();
        TestCase.assertTrue(netWorkScheduleTask.isMetricsEnabled(config));
        TestCase.assertTrue(netWorkScheduleTask.isPrepared(config));
        TestCase.assertNotNull(netWorkScheduleTask.getMetrics());

        DiskIoScheduleTask diskIoScheduleTask = new DiskIoScheduleTask();
        TestCase.assertTrue(diskIoScheduleTask.isMetricsEnabled(config));
        TestCase.assertTrue(diskIoScheduleTask.isPrepared(config));
        TestCase.assertNotNull(diskIoScheduleTask.getMetrics());

        DiskSpaceScheduleTask diskSpaceScheduleTask = new DiskSpaceScheduleTask();
        TestCase.assertTrue(diskSpaceScheduleTask.isMetricsEnabled(config));
        TestCase.assertTrue(diskSpaceScheduleTask.isPrepared(config));
        TestCase.assertNotNull(diskSpaceScheduleTask.getMetrics());


    }
}
