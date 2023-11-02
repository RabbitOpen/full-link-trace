package rabbit.flt.plugins.metrics.jna.task;

import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.metrics.NetworkMetrics;
import rabbit.flt.plugins.metrics.jna.loader.NetWorkLoader;

import java.util.ArrayList;
import java.util.List;

public class NetWorkScheduleTask extends ScheduleTask<NetworkMetrics> {

    private Long lastSampleTime = 0L;

    private NetWorkLoader netWorkLoader;

    private List<String> netCards = new ArrayList<>();

    @Override
    public boolean isPrepared(AgentConfig config) {
        if (netCards.isEmpty()) {
            for (String netCard : config.getNetMetricsCards().split(",")) {
                netCards.add(netCard.trim());
            }
            if (netCards.isEmpty()) {
                return false;
            }
        }
        if (null == netWorkLoader) {
            try {
                netWorkLoader = new NetWorkLoader();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        long interval = config.getNetworkSampleIntervalSeconds() * 1000L;
        long sampleTime = System.currentTimeMillis() / interval * interval;
        if (lastSampleTime != sampleTime) {
            lastSampleTime = sampleTime;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMetricsEnabled(AgentConfig config) {
        return config.isNetMetricsEnabled();
    }

    @Override
    public NetworkMetrics getMetrics() {
        if (null == netWorkLoader) {
            return null;
        }
        NetworkMetrics metrics = netWorkLoader.load(this.netCards);
        metrics.setSamplingTime(lastSampleTime);
        return metrics;
    }
}
