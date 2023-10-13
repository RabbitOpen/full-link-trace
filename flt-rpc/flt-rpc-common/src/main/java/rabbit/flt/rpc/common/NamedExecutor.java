package rabbit.flt.rpc.common;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedExecutor extends ThreadPoolExecutor {

    private static final AtomicInteger threadNumber = new AtomicInteger(1);

    private NamedExecutor(int poolSize, String namePrefix) {
        this(poolSize, poolSize, 30, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        setThreadFactory(new DefaultThreadFactory(namePrefix));
    }

    public NamedExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                         BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        setThreadFactory(new DefaultThreadFactory());
    }

    public static NamedExecutor fixedThreadsPool(int poolSize, String namePrefix) {
        return new NamedExecutor(poolSize, namePrefix);
    }

    @Override
    public void shutdown() {
        try {
            super.shutdown();
            awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    private class DefaultThreadFactory implements ThreadFactory {
        private ThreadGroup group;
        private String namePrefix;

        public DefaultThreadFactory() {
            this("named-pool-thread-");
        }

        private DefaultThreadFactory(String namePrefix) {
            SecurityManager manager = System.getSecurityManager();
            group = Objects.isNull(manager) ? Thread.currentThread().getThreadGroup() : manager.getThreadGroup();
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            return thread;
        }
    }
}
