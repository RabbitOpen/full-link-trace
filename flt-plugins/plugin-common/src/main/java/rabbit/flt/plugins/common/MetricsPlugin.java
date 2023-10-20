package rabbit.flt.plugins.common;

import rabbit.flt.common.*;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 指标插件
 */
public abstract class MetricsPlugin {

    private static Logger logger = AgentLoggerFactory.getLogger("metricsPlugin");

    private Thread thread;

    private Semaphore semaphore = new Semaphore(0);

    protected static MetricsDataHandler dataHandler;

    private static volatile boolean started = false;

    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * 启动任务
     */
    public void start() {
        initDataHandler();
        thread = new Thread(() -> {
            List<ScheduleTask<? extends Metrics>> tasks = getTasks();
            while (true) {
                try {
                    if (semaphore.tryAcquire(1, 2, TimeUnit.SECONDS)) {
                        break;
                    } else {
                        if (isFactoryPrepared()) {
                            scheduleTasks(tasks);
                        }
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> close()));
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 调度任务
     *
     * @param tasks
     */
    private void scheduleTasks(List<ScheduleTask<? extends Metrics>> tasks) {
        AgentConfig config = AbstractConfigFactory.getConfig();
        for (ScheduleTask<? extends Metrics> task : tasks) {
            if (!isMetricsEnabled(config, task) || !isPrepared(config, task)) {
                continue;
            }
            if (dataHandler.isMetricsEnabled(task.getMetricsType())) {
                // 远端允许上报才上报
                Metrics metrics = task.getMetrics();
                if (null != metrics) {
                    task.handle(dataHandler, metrics);
                }
            }
        }
    }

    /**
     * 获取任务列表
     *
     * @return
     */
    protected abstract List<ScheduleTask<? extends Metrics>> getTasks();

    /**
     * 判断任务是否就绪
     *
     * @param config
     * @param task
     * @return
     */
    protected boolean isPrepared(AgentConfig config, ScheduleTask<? extends Metrics> task) {
        return task.isPrepared(config);
    }

    /**
     * 是否允许上报metrics
     *
     * @param config
     * @param task
     * @return
     */
    protected boolean isMetricsEnabled(AgentConfig config, ScheduleTask<? extends Metrics> task) {
        return task.isMetricsEnabled(config);
    }

    /**
     * 初始化数据处理器
     */
    protected void initDataHandler() {
        if (null == dataHandler) {
            ServiceLoader<MetricsDataHandler> loader = ServiceLoader.load(MetricsDataHandler.class);
            for (MetricsDataHandler metricsDataHandler : loader) {
                if (null == dataHandler) {
                    dataHandler = metricsDataHandler;
                }
                if (metricsDataHandler.getPriority() < dataHandler.getPriority()) {
                    dataHandler = metricsDataHandler;
                }
            }
        }
    }

    /**
     * 关闭插件
     */
    private void close() {
        try {
            semaphore.release();
            thread.join();
            logger.info("{} is closed!", getClass().getSimpleName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected static boolean isFactoryPrepared() {
        return null != AbstractConfigFactory.getConfig();
    }

    /**
     * 尝试启动收集任务
     */
    public static void tryStartingAllPlugins() {
        if (started) {
            return;
        }
        try {
            lock.lock();
            if (started || !isFactoryPrepared()) {
                return;
            }
            logger.info("metric plugins are started!");
            ServiceLoader<MetricsPlugin> plugins = ServiceLoader.load(MetricsPlugin.class);
            for (MetricsPlugin plugin : plugins) {
                plugin.start();
            }
            started = true;
        } finally {
            lock.unlock();
        }
    }
}
