package rabbit.flt.plugins.common;

import rabbit.flt.common.*;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.trace.TraceData;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 内置链路数据处理器
 */
public class DefaultTraceDataHandler {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    private List<Thread> dataHandlerThreads = new ArrayList<>();

    private LinkedQueue<TraceData> dataQueue = new LinkedQueue<>(1024 * 256);

    private ReentrantLock lock = new ReentrantLock();

    private ReentrantLock warnLock = new ReentrantLock();

    private TraceDataHandler dataHandler;

    private Semaphore semaphore = new Semaphore(0);

    /**
     * 上次告警时间
     */
    private long lastWarnTime = 0L;

    private TraceInterceptor interceptor = null;

    public DefaultTraceDataHandler() {
        ServiceLoader<TraceInterceptor> loader = ServiceLoader.load(TraceInterceptor.class);
        for (TraceInterceptor traceInterceptor : loader) {
            interceptor = traceInterceptor;
            return;
        }
    }

    private void initDataHandlerThreads() {
        try {
            lock.lock();
            if (!dataHandlerThreads.isEmpty()) {
                return;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
            AbstractConfigFactory factory = AbstractConfigFactory.getFactory();
            factory.doInitialize();
            AgentConfig config = AbstractConfigFactory.getConfig();
            dataQueue.setCapacity(config.getMaxQueueSize());
            initDataHandler();
        } finally {
            lock.unlock();
        }
    }

    private void startReportThreads(AgentConfig config) {
        int threads = config.getMaxReportThreads();
        threads = threads > 1 ? threads : 1;

        logger.info("report metrics by [{}] threads", threads);
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(getRunnable(config), "trace-report-thread-".concat(Integer.toString(i)));
            thread.setDaemon(true);
            thread.start();
            dataHandlerThreads.add(thread);
        }
    }

    /**
     * 发送逻辑
     * @param config
     * @return
     */
    private Runnable getRunnable(AgentConfig config) {
        return () -> {
            long threshold = (long) (config.getMaxQueueSize() * config.getThreshold());
            while (true) {
                try {
                    if (semaphore.tryAcquire(1, 10, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                    while (true) {
                        List<TraceData> list = new ArrayList<>();
                        dataQueue.drainTo(list, config.getMaxTransportBatchSize(), 200);
                        if (list.isEmpty()) {
                            break;
                        }
                        int left = dataQueue.getLength();
                        if (config.showQueueLength() && left > threshold) {
                            logger.info("queue capacity is [{}], left data size is [{}], batch size is [{}]",
                                    config.getMaxQueueSize(), left, list.size());
                        }
                        list.forEach(t -> t.setApplicationCode(config.getApplicationCode()));
                        dataHandler.process(list);
                        dataHandler.discard(list);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        };
    }

    /**
     * 关闭线程
     */
    private void shutdown() {
        try {
            logger.info("trace data handler is closing........");
            semaphore.release(dataHandlerThreads.size());
            for (Thread thread : dataHandlerThreads) {
                thread.join();
            }
            dataHandler.close();
            logger.info("trace data handler is closed!");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 处理trace
     * @param traceData
     */
    public void process(TraceData traceData) {
        if (dataHandlerThreads.isEmpty()) {
            // 首次调用初始化
            initDataHandlerThreads();
            MetricsPlugin.tryStartingAllPlugins();
        }
        if (AbstractConfigFactory.getConfig().isMetricsOnly()) {
            // 只上报metrics
            return;
        }
        try {
            if (null != interceptor) {
                if (interceptor.filter(traceData)) {
                    dataQueue.add(traceData);
                }
            } else {
                dataQueue.add(traceData);
            }
        } catch (Exception e) {
            loggerWarnMessage(e.getMessage());
        }
    }

    private void loggerWarnMessage(String message) {
        if (warnLock.tryLock()) {
            if (System.currentTimeMillis() - lastWarnTime > 30L *1000) {
                logger.warn(message);
                lastWarnTime = System.currentTimeMillis();
            }
            warnLock.unlock();
        }
    }

    /**
     * 初始化data handler, 取优先级最高的使用
     */
    private void initDataHandler() {
        ServiceLoader<TraceDataHandler> loader = ServiceLoader.load(TraceDataHandler.class);
        for (TraceDataHandler handler : loader) {
            if (null == dataHandler) {
                dataHandler = handler;
            } else {
                if (handler.getPriority() < dataHandler.getPriority()) {
                    dataHandler = handler;
                }
            }
        }
    }
}
