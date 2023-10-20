package rabbit.flt.rpc.client;

import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.rpc.client.pool.ConfigBuilder;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.rpc.DataService;
import rabbit.flt.rpc.common.rpc.ProtocolService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DataHandler {

    protected Logger logger = AgentLoggerFactory.getLogger(getClass());

    /**
     * 全局唯一
     */
    private static final AgentRequestFactory requestFactory = new AgentRequestFactory();

    private DataService dataService;

    private ProtocolService protocolService;

    private ReentrantLock lock = new ReentrantLock();

    protected DataService getDataService() {
        if (null != dataService) {
            return dataService;
        }
        try {
            lock.lock();
            dataService = proxyService(DataService.class);
            return dataService;
        } finally {
            lock.unlock();
        }
    }

    protected ProtocolService getProtocolService() {
        if (null != protocolService) {
            return protocolService;
        }
        try {
            lock.lock();
            protocolService = proxyService(ProtocolService.class);
            return protocolService;
        } finally {
            lock.unlock();
        }
    }

    private <T> T proxyService(Class<T> clz) {
        AgentConfig config = AbstractConfigFactory.getConfig();
        if (null == config) {
            return null;
        }
        String servers = config.getServers();
        List<ServerNode> nodes = new ArrayList<>();
        for (String server : servers.split(",")) {
            String[] split = server.trim().split(":");
            ServerNode node = new ServerNode(split[0].trim(), Integer.parseInt(split[1].trim()));
            if (0 == nodes.stream().filter(n -> n.isSameNode(node)).count()) {
                nodes.add(node);
            }
        }
        // 幂等初始化
        requestFactory.init(ConfigBuilder.builder()
                .serverNodes(nodes)
                .rpcRequestTimeoutSeconds(config.getRpcRequestTimeoutSeconds())
                .connectionsPerServer(config.getMaxReportConnections())
                .applicationCode(config.getApplicationCode())
                .password(config.getSecurityKey())
                .build());
        return requestFactory.proxy(clz);
    }
}
