package rabbit.flt.rpc.client.pool;

import rabbit.flt.rpc.common.ServerNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfigBuilder {

    private List<Consumer<PoolConfig>> operations = new ArrayList<>();

    private ConfigBuilder() {}

    public static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    /**
     * do build
     * @return
     */
    public PoolConfig build() {
        PoolConfig poolConfig = new PoolConfig();
        operations.forEach(consumer -> consumer.accept(poolConfig));
        return poolConfig;
    }

    /**
     * 设置boss线程数
     * @param bossThreadCount
     * @return
     */
    public ConfigBuilder bossThreadCount(int bossThreadCount) {
        operations.add(c -> c.setBossThreadCount(bossThreadCount));
        return this;
    }

    /**
     * 设置worker线程数
     * @param workerThreadCount
     * @return
     */
    public ConfigBuilder workerThreadCount(int workerThreadCount) {
        operations.add(c -> c.setWorkerThreadCount(workerThreadCount));
        return this;
    }

    /**
     * 设置应用编码
     * @param applicationCode
     * @return
     */
    public ConfigBuilder applicationCode(String applicationCode) {
        operations.add(c -> c.setApplicationCode(applicationCode));
        return this;
    }

    /**
     * 设置rpc请求超时时间
     * @param rpcRequestTimeoutSeconds
     * @return
     */
    public ConfigBuilder rpcRequestTimeoutSeconds(int rpcRequestTimeoutSeconds) {
        operations.add(c -> c.setRpcRequestTimeoutSeconds(rpcRequestTimeoutSeconds));
        return this;
    }

    /**
     * 设置rpc最大重试次数
     * @param maxRetryTime
     * @return
     */
    public ConfigBuilder maxRetryTime(int maxRetryTime) {
        operations.add(c -> c.setMaxRetryTime(maxRetryTime));
        return this;
    }

    /**
     * 设置应用密码
     * @param password
     * @return
     */
    public ConfigBuilder password(String password) {
        operations.add(c -> c.setPassword(password));
        return this;
    }

    /**
     * 设置服务器节点
     * @param serverNodes
     * @return
     */
    public ConfigBuilder serverNodes(List<ServerNode> serverNodes) {
        operations.add(c -> c.setServerNodes(serverNodes));
        return this;
    }

    /**
     * 设置每个服务器的连接数
     * @param connectionsPerServer
     * @return
     */
    public ConfigBuilder connectionsPerServer(int connectionsPerServer) {
        operations.add(c -> c.setConnectionsPerServer(connectionsPerServer));
        return this;
    }
}
