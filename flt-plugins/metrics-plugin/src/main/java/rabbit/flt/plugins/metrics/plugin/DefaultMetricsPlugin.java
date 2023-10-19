package rabbit.flt.plugins.metrics.plugin;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.ScheduleTask;
import rabbit.flt.plugins.common.MetricsPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class DefaultMetricsPlugin extends MetricsPlugin {

    @Override
    protected List<ScheduleTask<? extends Metrics>> getTasks() {
        List<ScheduleTask<? extends Metrics>> scheduleTasks = new ArrayList<>();
        ServiceLoader<ScheduleTask> loader = ServiceLoader.load(ScheduleTask.class);
        for (ScheduleTask task : loader) {
            scheduleTasks.add(task);
        }
        return scheduleTasks;
    }
}
