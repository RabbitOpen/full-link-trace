package rabbit.flt.rpc.client.handler;

import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsDataHandler;
import rabbit.flt.common.metrics.*;
import rabbit.flt.rpc.client.DataHandler;
import rabbit.flt.rpc.common.rpc.ProtocolService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RpcMetricsDataHandler extends DataHandler implements MetricsDataHandler {

    /**
     * 缓存远端是否允许上报的配置
     */
    private Map<Class, Switch> switchMap = new ConcurrentHashMap<>();

    private Map<Class<? extends Metrics>, Consumer<Metrics>> consumerMap = new ConcurrentHashMap<>();

    public RpcMetricsDataHandler() {
        consumerMap.put(GcMetrics.class, d -> getDataService().handleGcMetrics((GcMetrics) d));
        consumerMap.put(MemoryMetrics.class, d -> getDataService().handleMemoryMetrics((MemoryMetrics) d));
        consumerMap.put(CpuMetrics.class, d -> getDataService().handleCpuMetrics((CpuMetrics) d));
        consumerMap.put(DiskIoMetrics.class, d -> getDataService().handleDiskIoMetrics((DiskIoMetrics) d));
        consumerMap.put(DiskSpaceMetrics.class, d -> getDataService().handleDiskSpaceMetrics((DiskSpaceMetrics) d));
        consumerMap.put(NetworkMetrics.class, d -> getDataService().handleNetworkMetrics((NetworkMetrics) d));
    }

    @Override
    public boolean handle(Metrics data) {
        if (data instanceof EnvironmentMetrics) {
            return getDataService().handleEnvironmentMetrics((EnvironmentMetrics) data);
        }
        consumerMap.get(data.getClass()).accept(data);
        return true;
    }

    @Override
    public boolean isMetricsEnabled(Class<? extends Metrics> type) {
        ProtocolService protocolService = getProtocolService();
        if (null == protocolService) {
            return false;
        }
        Switch aSwitch = switchMap.computeIfAbsent(type, t -> {
            AgentConfig config = AbstractConfigFactory.getConfig();
            boolean enabled = getProtocolService().isMetricsEnabled(config.getApplicationCode(), t);
            return new Switch(enabled);
        });
        if (System.currentTimeMillis() - aSwitch.getUpdateTime() > 5L * 60 * 1000) {
            // 5分钟更新一次（非严格）
            switchMap.remove(type);
        }
        return aSwitch.isEnable();
    }

    private class Switch {
        private boolean enable;

        private long updateTime;

        public Switch(boolean enable) {
            this.enable = enable;
            this.updateTime = System.currentTimeMillis();
        }

        public boolean isEnable() {
            return enable;
        }

        public long getUpdateTime() {
            return updateTime;
        }
    }
}
