package rabbit.flt.plugins.metrics.task;

import com.sun.management.GarbageCollectionNotificationInfo;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.common.metrics.GcMetrics;

import javax.management.NotificationBroadcaster;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class GcScheduleTask extends ScheduleTask<GcMetrics> {

    private BlockingQueue<GcMetrics> gcList = new ArrayBlockingQueue<>(512);

    // 总次数计数
    private Map<String, AtomicLong> timeCounter = new ConcurrentHashMap<>();

    // 总耗时计数
    private Map<String, AtomicLong> costCounter = new ConcurrentHashMap<>();

    private long lastMinorGcTime = 0;

    public GcScheduleTask() {
        List<GarbageCollectorMXBean> beanList = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean mxBean : beanList) {
            NotificationBroadcaster broadcaster = (NotificationBroadcaster) mxBean;
            broadcaster.addNotificationListener((notification, beanName) -> {
                CompositeDataSupport support = (CompositeDataSupport) notification.getUserData();
                GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from(support);
                GcMetrics metrics = new GcMetrics();
                metrics.setCause(info.getGcCause());
                metrics.setCost(info.getGcInfo().getDuration());
                metrics.setGcAction(info.getGcAction());
                metrics.setGcName(info.getGcName());
                metrics.setStart(System.currentTimeMillis() - metrics.getCost());
                Map<String, String> detail = new HashMap<>();
                Map<String, MemoryUsage> afterGc = info.getGcInfo().getMemoryUsageAfterGc();
                Map<String, MemoryUsage> beforeGc = info.getGcInfo().getMemoryUsageBeforeGc();
                for (Map.Entry<String, MemoryUsage> entry : afterGc.entrySet()) {
                    String key = entry.getKey();
                    StringBuilder sb = new StringBuilder(beforeGc.get(key).getUsed() / M + "M");
                    sb.append(" --> ").append(entry.getValue().getUsed() / M).append("M");
                    sb.append(" (max: ").append(entry.getValue().getMax() / M).append("M)");
                    detail.put(key, sb.toString());
                }
                AtomicLong timeCounter = this.timeCounter.computeIfAbsent(info.getGcName(), k -> new AtomicLong(0));
                metrics.setTotal(timeCounter.incrementAndGet());
                AtomicLong costCounter = this.costCounter.computeIfAbsent(info.getGcName(), k -> new AtomicLong(0));
                metrics.setTotal(costCounter.addAndGet(metrics.getCost()));
                metrics.setDetail(detail);
                cacheGcMetrics(metrics);
            }, null, mxBean.getName());
        }
    }

    private void cacheGcMetrics(GcMetrics metrics) {
        if ("end of minor GC".equalsIgnoreCase(metrics.getGcAction())) {
            long now = System.currentTimeMillis();
            int gcReportIntervalSeconds = 5;
            AgentConfig config = AbstractConfigFactory.getConfig();
            if (null != config) {
                gcReportIntervalSeconds = config.getGcReportIntervalSeconds();
            }
            if (now - lastMinorGcTime < gcReportIntervalSeconds * 1000L) {
                return;
            }
            lastMinorGcTime = now;
        }
        try {
            gcList.add(metrics);
        } catch (Exception e) {
            // discard
        }
    }

    @Override
    public boolean isPrepared(AgentConfig config) {
        return gcList.size() > 0;
    }

    @Override
    public boolean isMetricsEnabled(AgentConfig config) {
        return config.isGcMetricsEnabled();
    }

    @Override
    public GcMetrics getMetrics() {
        try {
            return gcList.poll(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }
}
