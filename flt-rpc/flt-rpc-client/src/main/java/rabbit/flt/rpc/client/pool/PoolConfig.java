package rabbit.flt.rpc.client.pool;

import rabbit.flt.rpc.client.RpcRequestInterceptor;
import rabbit.flt.rpc.common.ServerNode;

import java.util.ArrayList;
import java.util.List;

public class PoolConfig {

    /**
     * boss线程数
     */
    private int bossThreadCount = 1;

    private int workerThreadCount = 1;

    /**
     * 获取客户端超时秒数
     */
    private int acquireClientTimeoutSeconds = 3;

    /**
     * rpc 请求超时时长
     */
    private int rpcRequestTimeoutSeconds = 30;

    /**
     * rpc 最大重试次数
     */
    private int maxRetryTime = 3;

    /**
     * 服务端节点
     */
    private List<ServerNode> serverNodes = new ArrayList<>();

    /**
     * 事件监听器
     */
    private ChannelListener channelListener = channel -> {
        // do nothing
    };

    /**
     * 每个服务器节点上的连接数
     */
    private int connectionsPerServer = 1;

    private String applicationCode;

    private String password;

    /**
     * 心跳间隔
     */
    private int keepAliveIntervalSeconds = 30;

    private RpcRequestInterceptor requestInterceptor;

    public int getBossThreadCount() {
        return bossThreadCount;
    }

    public void setBossThreadCount(int bossThreadCount) {
        this.bossThreadCount = bossThreadCount;
    }

    public int getWorkerThreadCount() {
        return workerThreadCount;
    }

    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public int getAcquireClientTimeoutSeconds() {
        return acquireClientTimeoutSeconds;
    }

    public void setAcquireClientTimeoutSeconds(int acquireClientTimeoutSeconds) {
        this.acquireClientTimeoutSeconds = acquireClientTimeoutSeconds;
    }

    public int getRpcRequestTimeoutSeconds() {
        return rpcRequestTimeoutSeconds;
    }

    public void setRpcRequestTimeoutSeconds(int rpcRequestTimeoutSeconds) {
        this.rpcRequestTimeoutSeconds = rpcRequestTimeoutSeconds;
    }

    public int getMaxRetryTime() {
        return maxRetryTime;
    }

    public void setMaxRetryTime(int maxRetryTime) {
        this.maxRetryTime = maxRetryTime;
    }

    public List<ServerNode> getServerNodes() {
        return serverNodes;
    }

    /**
     * 无重复添加
     * @param serverNodes
     */
    public void setServerNodes(List<ServerNode> serverNodes) {
        for (ServerNode serverNode : serverNodes) {
            if (!isExist(serverNode)) {
                this.serverNodes.add(serverNode);
            }
        }
    }

    private boolean isExist(ServerNode serverNode) {
        for (ServerNode node : this.serverNodes) {
            if (node.isSameNode(serverNode)) {
                return true;
            }
        }
        return false;
    }

    public ChannelListener getChannelListener() {
        return channelListener;
    }

    public void setChannelListener(ChannelListener channelListener) {
        this.channelListener = channelListener;
    }

    public int getConnectionsPerServer() {
        return connectionsPerServer;
    }

    public void setConnectionsPerServer(int connectionsPerServer) {
        this.connectionsPerServer = connectionsPerServer;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getKeepAliveIntervalSeconds() {
        return keepAliveIntervalSeconds;
    }

    public void setKeepAliveIntervalSeconds(int keepAliveIntervalSeconds) {
        this.keepAliveIntervalSeconds = keepAliveIntervalSeconds;
    }

    public RpcRequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    public void setRequestInterceptor(RpcRequestInterceptor requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }
}
