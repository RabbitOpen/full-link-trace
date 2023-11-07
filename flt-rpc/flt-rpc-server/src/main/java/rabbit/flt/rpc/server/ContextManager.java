package rabbit.flt.rpc.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.rpc.common.Attributes;
import rabbit.flt.rpc.common.Serializer;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ContextManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<SelectionKey, Long> selectionKeyCache = new ConcurrentHashMap<>();

    private Thread monitor;

    private Semaphore quit = new Semaphore(0);

    private Server server;

    protected ContextManager(Server server) {
        this.server = server;
        monitor = new Thread(() -> {
            while (true) {
                try {
                    if (quit.tryAcquire(1, 10, TimeUnit.SECONDS)) {
                        break;
                    }
                    selectionKeyCache.forEach((key, lastActiveTime) -> {
                        long maxIdleTime = server.getMaxIdleSeconds() * 1000L;
                        if (System.currentTimeMillis() - lastActiveTime > maxIdleTime) {
                            logger.info("close dead client: {}", getRemoteAddress(key));
                            selectionKeyCache.remove(key);
                            closeKey(key);
                        }
                    });
                } catch (Exception e) {
                    // ignore
                }
            }
        });
        monitor.setDaemon(false);
        monitor.start();
    }

    protected void closeKey(SelectionKey key) {
        if (null == key) {
            return;
        }
        Map<String, Object> attrs = (Map<String, Object>) key.attachment();
        ReentrantLock lock = (ReentrantLock) attrs.get(Attributes.WRITE_LOCK);
        try {
            lock.lock();
            server.getClientEventHandler().onClientClosed(key);
            key.cancel();
            key.channel().close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    private SocketAddress getRemoteAddress(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            return channel.getRemoteAddress();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 更新活跃时间
     * @param key
     */
    public void active(SelectionKey key) {
        selectionKeyCache.put(key, System.currentTimeMillis());
    }

    public void inActive(SelectionKey key) {
        selectionKeyCache.remove(key);
        closeKey(key);
    }

    /**
     * 关闭
     */
    public void close() {
        quit.release();
        selectionKeyCache.forEach((key, t) -> closeKey(key));
        try {
            monitor.join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
