package rabbit.flt.plugins.metrics.jna.task;

import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.metrics.DiskSpaceMetrics;
import rabbit.flt.common.metrics.info.SpaceInfo;
import rabbit.flt.common.utils.StringUtils;

import java.io.File;

public class DiskSpaceScheduleTask extends ScheduleTask<DiskSpaceMetrics> {

    private Long lastSampleTime = 0L;

    private String diskDirs;

    @Override
    public boolean isPrepared(AgentConfig config) {
        if (null == this.diskDirs) {
            String diskSpaceMetricsDirs = config.getDiskSpaceMetricsDirs();
            if (StringUtils.isEmpty(diskSpaceMetricsDirs)) {
                return false;
            }
            this.diskDirs = diskSpaceMetricsDirs;
        }
        long interval = config.getDiskSpaceSampleIntervalSeconds() * 1000L;
        long sampleTime = System.currentTimeMillis() / interval * interval;
        if (lastSampleTime != sampleTime) {
            lastSampleTime = sampleTime;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMetricsEnabled(AgentConfig config) {
        return config.isDiskMetricsEnabled();
    }

    @Override
    public DiskSpaceMetrics getMetrics() {
        DiskSpaceMetrics metrics = new DiskSpaceMetrics();
        metrics.setSamplingTime(lastSampleTime);
        for (String dir : diskDirs.split(",")) {
            File file = new File(dir.trim());
            if (!file.exists() || !file.isDirectory()) {
                continue;
            }
            SpaceInfo spaceInfo = new SpaceInfo();
            spaceInfo.setDir(dir.trim());
            spaceInfo.setTotal(file.getTotalSpace());
            spaceInfo.setUsed(file.getTotalSpace() - file.getFreeSpace());
            metrics.getSpaceInfoList().add(spaceInfo);
        }
        return metrics;
    }
}
