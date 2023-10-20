package rabbit.flt.plugins.metrics.jna.loader;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import rabbit.flt.common.metrics.NetworkMetrics;
import rabbit.flt.common.metrics.info.NetFlowInfo;
import rabbit.flt.plugins.metrics.jna.AbstractLoader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

public class NetWorkLoader extends AbstractLoader {

    private HardwareAbstractionLayer hardware;

    private Map<String, NetDetail> lastDetail = new ConcurrentHashMap<>();

    public NetWorkLoader() {
        hardware = new SystemInfo().getHardware();
    }

    public NetworkMetrics load(List<String> netCards) {
        NetworkMetrics metrics = new NetworkMetrics();
        List<NetworkIF> networks = getNetworkIFs();
        for (NetworkIF n : networks) {
            if (!netCards.contains(n.getName())) {
                continue;
            }
            NetDetail now = new NetDetail(n.getPacketsRecv(), n.getPacketsSent(), n.getBytesSent(), n.getBytesRecv());
            NetDetail last = lastDetail.get(n.getName());
            lastDetail.put(n.getName(), now);
            if (null == last || last.getSampleTime() == now.getSampleTime()) {
                LockSupport.parkNanos(10L * 1000 * 1000);
                return load(netCards);
            }
            NetFlowInfo delta = now.delta(last);
            delta.setNetCard(n.getName());
            metrics.getFlowInfoList().add(delta);
        }
        return metrics;
    }

    public List<NetworkIF> getNetworkIFs() {
        return hardware.getNetworkIFs();
    }

    private class NetDetail {

        private long rxPks;

        private long txPks;

        private long txBytes;

        private long rxBytes;

        // 采样时间
        private long sampleTime;

        public NetDetail(long rxPks, long txPks, long txBytes, long rxBytes) {
            this.rxPks = rxPks;
            this.txPks = txPks;
            this.txBytes = txBytes;
            this.rxBytes = rxBytes;
            this.sampleTime = System.currentTimeMillis();
        }

        private NetFlowInfo delta(NetDetail old) {
            double delta = new Double(getSampleTime() - old.getSampleTime()) / 1000;
            NetFlowInfo flowInfo = new NetFlowInfo();
            flowInfo.setTxPktPerSec(roundHalfUp((getTxPks() - old.getTxPks()) / delta, 2));
            flowInfo.setRxPktPerSec(roundHalfUp((getRxPks() - old.getRxPks()) / delta, 2));
            flowInfo.setTxBytesPerSec(Math.floor((getTxBytes() - old.getTxBytes()) / delta));
            flowInfo.setRxBytesPerSec(Math.floor((getRxBytes() - old.getRxBytes()) / delta));
            return flowInfo;
        }

        public long getRxPks() {
            return rxPks;
        }

        public long getTxPks() {
            return txPks;
        }

        public long getTxBytes() {
            return txBytes;
        }

        public long getRxBytes() {
            return rxBytes;
        }

        public long getSampleTime() {
            return sampleTime;
        }
    }
}
