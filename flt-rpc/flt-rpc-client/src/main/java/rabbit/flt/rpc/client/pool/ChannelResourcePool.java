package rabbit.flt.rpc.client.pool;

import rabbit.flt.common.utils.CollectionUtils;
import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.common.NamedExecutor;
import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.SelectorResetListener;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.exception.AuthenticationException;
import rabbit.flt.rpc.common.exception.NoPreparedClientException;
import rabbit.flt.rpc.common.exception.RpcTimeoutException;
import rabbit.flt.rpc.common.exception.UnRegisteredHandlerException;
import rabbit.flt.rpc.common.nio.AbstractClientChannel;
import rabbit.flt.rpc.common.nio.ChannelProcessor;
import rabbit.flt.rpc.common.nio.SelectorWrapper;
import rabbit.flt.rpc.common.rpc.RpcRequest;
import reactor.core.publisher.Mono;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ChannelResourcePool extends AbstractClientChannel implements Client {

    /**
     * 业务线程池
     */
    private ExecutorService workerExecutor;

    /**
     * io线程池，不可执行长期阻塞的任务
     */
    private ExecutorService bossExecutor;

    /**
     * 回调任务线程池
     */
    private ExecutorService callbackExecutor;

    private ChannelProcessor channelProcessor;

    /**
     * 资源守卫
     */
    private ResourceGuard resourceGuard;

    // 操作锁
    private ReentrantLock lock = new ReentrantLock();

    // 选取client时的游标
    private int cursor = 0;

    /**
     * 客户端连接
     */
    private List<ClientChannel> clientChannelList = new ArrayList<>();

    /**
     * 配置
     */
    private PoolConfig poolConfig;

    /**
     * 初始化
     *
     * @param config
     */
    public void init(PoolConfig config) {
        try {
            this.poolConfig = config;
            if (CollectionUtils.isEmpty(poolConfig.getServerNodes())) {
                throw new RpcException("server nodes can't be empty");
            }
            resourceGuard = new ResourceGuard(this);
            callbackExecutor = NamedExecutor.fixedThreadsPool(1, "callback-task-pool-");
            workerExecutor = NamedExecutor.fixedThreadsPool(poolConfig.getWorkerThreadCount(), "worker-pool-");
            bossExecutor = NamedExecutor.fixedThreadsPool(poolConfig.getBossThreadCount(), "boss-pool-");
            selectorWrapper = new SelectorWrapper();
            channelProcessor = new ChannelProcessor(selectorWrapper, this);
            channelProcessor.start();
            initClients();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    /**
     * 初始化客户端连接
     */
    private void initClients() {
        try {
            lock.lock();
            for (int i = 0; i < this.poolConfig.getConnectionsPerServer(); i++) {
                poolConfig.getServerNodes().forEach(node -> this.clientChannelList.add(new ClientChannel(this, node)));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        // 守卫的关闭操作可能会涉及使用资源池，所以采取无锁关闭
        resourceGuard.close();
        try {
            lock.lock();
            channelProcessor.close();
            // 关闭所有连接
            this.clientChannelList.forEach(ClientChannel::close);
            bossExecutor.shutdown();
            workerExecutor.shutdown();
            callbackExecutor.shutdown();
            selectorWrapper.close();
            logger.info("channel resource pool is closed!");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void connectFailed(SelectionKey selectionKey, Throwable t) {
        getClientChannel(selectionKey).connectFailed(selectionKey, t);
    }

    @Override
    public void onServerConnected(SelectionKey selectionKey) {
        getClientChannel(selectionKey).onServerConnected(selectionKey);
    }

    @Override
    protected ServerNode getServerNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processChannelData(SelectionKey selectionKey, byte[] data, int transferSize) {
        getClientChannel(selectionKey).processChannelData(selectionKey, data, transferSize);
    }

    @Override
    public void disconnected(SelectionKey selectionKey) {
        getClientChannel(selectionKey).disconnected(selectionKey);
    }

    @Override
    public ExecutorService getBossExecutor() {
        return bossExecutor;
    }

    @Override
    public ExecutorService getWorkerExecutor() {
        return workerExecutor;
    }

    @Override
    public SelectorResetListener getSelectorResetListener() {
        return (oldKey, newKey) -> {
            SelectorResetListener resetListener = getClientChannel(oldKey).getSelectorResetListener();
            if (null != resetListener) {
                resetListener.keyChanged(oldKey, newKey);
            }
        };
    }

    /**
     * 刷新服务器节点
     *
     * @param nodeList
     */
    public void refreshServerNodes(List<ServerNode> nodeList) {
        List<ServerNode> unknownServers = new ArrayList<>();
        poolConfig.getServerNodes().forEach(n -> {
            boolean exist = false;
            for (ServerNode node : nodeList) {
                if (node.isSameNode(n)) {
                    exist = true;
                }
            }
            if (!exist) {
                unknownServers.add(n);
                logger.info("remove unknown server node [{}:{}]", n.getHost(), n.getPort());
            }
        });
        poolConfig.getServerNodes().removeAll(unknownServers);
        nodeList.forEach(n -> {
            for (ServerNode serverNode : poolConfig.getServerNodes()) {
                if (serverNode.isSameNode(n)) {
                    return;
                }
            }
            poolConfig.getServerNodes().add(n);
            logger.info("server node[{}:{}] is found!", n.getHost(), n.getPort());
            addClientsByServer(n);
        });
        removeInvalidClients();
    }

    @Override
    protected final void serverNodeClosed(ServerNode serverNode) {
        try {
            lock.lock();
            for (ClientChannel channel : getClientChannelList()) {
                if (channel.getServerNode().isSameNode(serverNode)) {
                    channel.disconnected(null);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除无效节点的连接
     */
    private void removeInvalidClients() {
        try {
            lock.lock();
            List<ClientChannel> clients2Remove = new ArrayList<>();
            for (ClientChannel client : clientChannelList) {
                if (isValidServerNode(client.getServerNode())) {
                    continue;
                }
                client.close();
                getResourceGuard().remove(client);
                clients2Remove.add(client);
            }
            clientChannelList.removeAll(clients2Remove);
            Collections.shuffle(this.clientChannelList);
        } finally {
            lock.unlock();
        }
    }

    public ResourceGuard getResourceGuard() {
        return resourceGuard;
    }

    /**
     * 判断节点是否有效
     *
     * @param target
     * @return
     */
    private boolean isValidServerNode(ServerNode target) {
        for (ServerNode serverNode : poolConfig.getServerNodes()) {
            if (target.isSameNode(serverNode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加连接
     *
     * @param serverNode
     */
    private void addClientsByServer(ServerNode serverNode) {
        try {
            lock.lock();
            for (int i = 0; i < poolConfig.getConnectionsPerServer(); i++) {
                this.clientChannelList.add(new ClientChannel(this, serverNode));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 发起请求
     *
     * @param request
     * @param timeoutSeconds
     * @return
     */
    @Override
    public <T> T doRequest(RpcRequest request, int timeoutSeconds) {
        if (request.isMonoRequest()) {
            return (T) doAsyncRequest(request, timeoutSeconds);
        } else {
            return (T) doSyncRequest(request, timeoutSeconds);
        }
    }

    /**
     * 同步请求
     *
     * @param request
     * @param timeoutSeconds
     * @return
     */
    private Object doSyncRequest(RpcRequest request, int timeoutSeconds) {
        try {
            return doRpcRequest(request, timeoutSeconds);
        } catch (NoPreparedClientException | UnRegisteredHandlerException | AuthenticationException | RpcTimeoutException e) {
            throw e;
        } catch (RpcException e) {
            if (request.getCounter() > request.getMaxRetryTimes()) {
                throw e;
            }
            return doRequest(request, timeoutSeconds);
        }
    }

    /**
     * 发起rpc请求
     *
     * @param request
     * @param timeoutSeconds
     * @return
     */
    private Object doRpcRequest(RpcRequest request, int timeoutSeconds) {
        request.increase();
        long timeoutMills = poolConfig.getAcquireClientTimeoutSeconds() * 1000L;
        return getClient(timeoutMills).doRequest(request, timeoutSeconds);
    }

    /**
     * 异步请求
     *
     * @param request
     * @param timeoutSeconds
     * @return
     */
    private Object doAsyncRequest(RpcRequest request, int timeoutSeconds) {
        return Mono.defer(() -> (Mono<?>) doRpcRequest(request, timeoutSeconds)).onErrorResume(e -> {
            if (e instanceof NoPreparedClientException || e instanceof UnRegisteredHandlerException
                    || e instanceof AuthenticationException || e instanceof RpcTimeoutException) {
                return Mono.error(e);
            } else {
                if (request.getCounter() > request.getMaxRetryTimes()) {
                    return Mono.error(e);
                }
                return doRequest(request, timeoutSeconds);
            }
        });
    }

    @Override
    public void doConnect() {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取就绪的连接
     *
     * @param timeoutMills
     * @return
     */
    private ClientChannel getClient(long timeoutMills) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                lock.lock();
                int size = clientChannelList.size();
                for (int i = cursor; i < cursor + size; i++) {
                    int index = i % size;
                    ClientChannel client = clientChannelList.get(index);
                    if (isClientPrepared(client)) {
                        cursor = (index + 1) % size;
                        return client;
                    }
                }
            } finally {
                lock.unlock();
            }
            if (System.currentTimeMillis() - start > timeoutMills) {
                throw new NoPreparedClientException();
            }
            LockSupport.parkNanos(3L * 1000 * 1000);
        }
    }

    protected boolean isClientPrepared(ClientChannel clientChannel) {
        return clientChannel.isPrepared();
    }

    public ExecutorService getCallbackExecutor() {
        return callbackExecutor;
    }

    public ChannelProcessor getChannelProcessor() {
        return channelProcessor;
    }

    public List<ClientChannel> getClientChannelList() {
        return clientChannelList;
    }

    public PoolConfig getPoolConfig() {
        return poolConfig;
    }
}
