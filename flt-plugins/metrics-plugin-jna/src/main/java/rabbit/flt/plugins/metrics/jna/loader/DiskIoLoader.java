package rabbit.flt.plugins.metrics.jna.loader;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import rabbit.flt.common.metrics.DiskIoMetrics;
import rabbit.flt.common.metrics.info.DiskIoInfo;
import rabbit.flt.plugins.metrics.jna.AbstractLoader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

public class DiskIoLoader extends AbstractLoader {

    private long lastMoment;

    private HardwareAbstractionLayer hardware;

    private Map<String, DiskIoInfo> lastInfo;

    public DiskIoLoader() {
        SystemInfo systemInfo = new SystemInfo();
        hardware = systemInfo.getHardware();
        lastMoment = 0;
        lastInfo = new ConcurrentHashMap<>();
    }

    public DiskIoMetrics load() {
        DiskIoMetrics metrics = new DiskIoMetrics();
        List<HWDiskStore> stores = hardware.getDiskStores();
        long current = System.currentTimeMillis();
        for (HWDiskStore store : stores) {
            DiskIoInfo now = getDiskIoInfo(store);
            DiskIoInfo last = lastInfo.get(store.getName());
            lastInfo.put(store.getName(), now);
            if (null == last) {
                lastMoment = current;
                LockSupport.parkNanos(10L * 1000 * 1000);
                return load();
            }
            DiskIoInfo delta = delta(now, last, current - lastMoment);
            metrics.getDiskIoInfoList().add(delta);
        }
        lastMoment = current;
        return metrics;
    }

    private DiskIoInfo delta(DiskIoInfo now, DiskIoInfo old, long delta) {
        DiskIoInfo info = new DiskIoInfo();
        info.setDeviceName(now.getDeviceName());
        info.setWriteBytesPerSec(secondRate(now.getWriteBytesPerSec(), old.getWriteBytesPerSec(), delta));
        info.setWriteTimesPerSec(secondRate(now.getWriteTimesPerSec(), old.getWriteTimesPerSec(), delta));
        info.setReadTimesPerSec(secondRate(now.getReadTimesPerSec(), old.getReadTimesPerSec(), delta));
        info.setReadBytesPerSec(secondRate(now.getReadBytesPerSec(), old.getReadBytesPerSec(), delta));
        return info;
    }

    private DiskIoInfo getDiskIoInfo(HWDiskStore store) {
        DiskIoInfo diskIoInfo = new DiskIoInfo();
        diskIoInfo.setDeviceName(store.getName());
        diskIoInfo.setWriteTimesPerSec(store.getWrites());
        diskIoInfo.setWriteBytesPerSec(store.getWriteBytes());
        diskIoInfo.setReadBytesPerSec(store.getReadBytes());
        diskIoInfo.setReadTimesPerSec(store.getReads());
        return diskIoInfo;
    }

    private long secondRate(long cur, long pre, long deltaMils) {
        long delta = cur - pre;
        double delSec = deltaMils / 1000.0;
        return (long) (roundHalfUp(delta / delSec / 10, 1) * 10);
    }
}
