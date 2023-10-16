package rabbit.flt.rpc.client.pool;

import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.client.RequestFactory;
import rabbit.flt.rpc.common.ChannelStatus;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.rpc.ProtocolService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static rabbit.flt.rpc.common.ChannelStatus.INIT;

public class ResourceGuard extends Thread {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    private Map<ClientChannel, ChannelStatus> cache = new ConcurrentHashMap<>();

    private boolean quit = false;

    private ChannelResourcePool pool;

    private ProtocolService protocolService;

    /**
     * 上次加载服务器列表时间
     */
    private long lastLoadServerTime = 0L;

    public ResourceGuard() {
        this(null);
    }

    public ResourceGuard(ChannelResourcePool pool) {
        this.pool = pool;
        setName("client-resource-guard");
        setDaemon(false);
        start();
        protocolService = new RequestFactory() {
            @Override
            protected Client getClient() {
                return pool;
            }
        }.proxy(ProtocolService.class);
    }

    @Override
    public void run() {
        while (true) {
            cache.forEach((client, status) -> {
                try {
                    if (INIT == client.getChannelStatus()) {
                        client.doConnect();
                    } else if (client.getChannelStatus().isConnected()) {
                        client.keepAlive();
                        updateServerNodes();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            });
            LockSupport.parkNanos(100 * 1000L * 1000);
            if (quit) {
                return;
            }
        }
    }

    /**
     * 更新服务端节点信息
     */
    private void updateServerNodes() {
        if (System.currentTimeMillis() - lastLoadServerTime < 60L * 1000) {
            return;
        }
        List<ServerNode> serverNodes = protocolService.getServerNodes();
        lastLoadServerTime = System.currentTimeMillis();
        pool.refreshServerNodes(serverNodes);
    }

    public void add(ClientChannel clientChannel) {
        cache.put(clientChannel, INIT);
    }

    /**
     * 唤醒线程
     */
    public void wakeup() {
        LockSupport.unpark(this);
    }

    public void close() {
        quit = true;
        wakeup();
        try {
            join();
            logger.info("resource guard is closed!");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 移除监控
     *
     * @param client
     */
    public void remove(Client client) {
        cache.remove(client);
    }

    public List<ClientChannel> getClientChannels() {
        return new ArrayList<>(cache.keySet());
    }
}
