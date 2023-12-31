package rabbit.flt.rpc.client.pool;

import rabbit.flt.common.utils.GZipUtils;
import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.client.RequestFactory;
import rabbit.flt.rpc.common.*;
import rabbit.flt.rpc.common.exception.IllegalChannelStatusException;
import rabbit.flt.rpc.common.exception.RpcTimeoutException;
import rabbit.flt.rpc.common.nio.AbstractClientChannel;
import rabbit.flt.rpc.common.nio.SelectorWrapper;
import rabbit.flt.rpc.common.rpc.KeepAlive;
import rabbit.flt.rpc.common.rpc.RpcRequest;
import rabbit.flt.rpc.common.rpc.RpcResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static rabbit.flt.rpc.common.Attributes.NIO_CLIENT;
import static rabbit.flt.rpc.common.ChannelStatus.*;

/**
 * 客户端
 */
public class ClientChannel extends AbstractClientChannel implements Client, KeepAlive {

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

    // 连接的服务端
    private ServerNode serverNode;

    private SocketChannel socketChannel;

    private ReentrantLock lock = new ReentrantLock();

    private SelectionKey selectionKey;

    private final Map<Long, RpcRequest> signals = new ConcurrentHashMap<>();

    /**
     * 资源守卫
     */
    private ResourceGuard resourceGuard;

    private ChannelListener channelListener;

    private KeepAlive keepAlive;

    private long lastKeepAliveTime = System.currentTimeMillis();

    /**
     * 上次泄露检查时间
     */
    private long lastLeakCheckingTime = 0L;

    /**
     * 上次连接时间，重连时使用，防止频繁重试
     */
    private long lastConnectTime = 0L;

    /**
     * 心跳间隔
     */
    private int keepAliveIntervalSeconds = 30;

    /**
     * 构造函数
     *
     * @param pool
     * @param serverNode
     */
    public ClientChannel(ChannelResourcePool pool, ServerNode serverNode) {
        this.workerExecutor = pool.getWorkerExecutor();
        this.bossExecutor = pool.getBossExecutor();
        this.serverNode = serverNode;
        this.resourceGuard = pool.getResourceGuard();
        this.selectorWrapper = pool.getWrapper();
        initKeepAlive(pool.getPoolConfig().getRpcRequestTimeoutSeconds());
        this.callbackExecutor = pool.getCallbackExecutor();
        this.keepAliveIntervalSeconds = pool.getPoolConfig().getKeepAliveIntervalSeconds();
        this.channelListener = pool.getPoolConfig().getChannelListener();
        this.resourceGuard.add(this);
    }

    /**
     * 构造函数
     * @param workerExecutor
     * @param bossExecutor
     * @param serverNode
     * @param guard
     * @param selectorWrapper
     */
    public ClientChannel(ExecutorService workerExecutor, ExecutorService bossExecutor, ServerNode serverNode,
                         ResourceGuard guard, SelectorWrapper selectorWrapper) {
        this.channelListener = channel -> {
            // do nothing
        };
        this.workerExecutor = workerExecutor;
        this.bossExecutor = bossExecutor;
        this.serverNode = serverNode;
        this.resourceGuard = guard;
        this.selectorWrapper = selectorWrapper;
        initKeepAlive(30);
        this.resourceGuard.add(this);
    }

    /**
     * 初始化心跳服务
     */
    private void initKeepAlive(int rpcTimeoutSeconds) {
        keepAlive = new RequestFactory() {
            @Override
            protected Client getClient() {
                return ClientChannel.this;
            }

            @Override
            protected int getMaxRetryTime() {
                return 0;
            }

            @Override
            protected int getRequestTimeoutSeconds() {
                return rpcTimeoutSeconds;
            }
        }.proxy(KeepAlive.class);
    }

    /**
     * 发送数据
     *
     *  |--4字节 内容长度--|-- 4字节 压缩标识 --|-- 4字节 明文内容的长度 --|-- n字节数据内容 --|
     *  |--                     12字节协议头                        --|--   数据内容   --|
     * @param request
     * @param <T>
     * @return
     */
    @Override
    public <T> T doRequest(RpcRequest request) {
        try {
            lock.lock();
            if (!getChannelStatus().isConnected()) {
                throw new IllegalChannelStatusException(getChannelStatus());
            }
            signals.put(request.getRequestId(), request);
            byte[] bytes = Serializer.serialize(request);
            boolean zipped = false;
            int originalSize = bytes.length;
            if (originalSize > 1024 * 256) {
                bytes = GZipUtils.zip(bytes);
                zipped = true;
            }
            ByteBuffer buffer = ByteBuffer.allocate(12 + bytes.length);
            buffer.putInt(bytes.length);
            buffer.putInt(zipped ? DataType.GZIPPED : DataType.UN_COMPRESSED);
            buffer.putInt(originalSize);
            buffer.put(bytes);
            buffer.position(0);
            while (buffer.position() != buffer.capacity()) {
                if (0 == socketChannel.write(buffer)) {
                    LockSupport.parkNanos(10L * 1000 * 1000);
                }
            }
        } catch (IllegalChannelStatusException e) {
            signals.remove(request.getRequestId());
            throw e;
        } catch (IOException e) {
            signals.remove(request.getRequestId());
            // 出现io错误直接disconnected
            disconnected(selectionKey);
            throw new RpcException(e);
        } finally {
            lock.unlock();
        }
        try {
            return (T) request.getResponse(() -> signals.remove(request.getRequestId()));
        } catch (RpcTimeoutException e) {
            // 超时断开重连
            disconnected(selectionKey);
            throw e;
        }
    }

    @Override
    public void doConnect() {
        try {
            lock.lock();
            if (INIT != getChannelStatus() || null == getWrapper()
                    || System.currentTimeMillis() - lastConnectTime < 5000) {
                return;
            }
            this.refreshLastConnectTime();
            if (null != socketChannel) {
                close(socketChannel);
                socketChannel = null;
                selectionKey.cancel();
            }
            getWrapper().addHookJob(() -> {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(false);
                    Map<String, Object> attr = new ConcurrentHashMap<>();
                    attr.put(NIO_CLIENT, this);
                    selectionKey = socketChannel.register(selectorWrapper.getSelector(),
                            SelectionKey.OP_CONNECT, attr);
                    socketChannel.connect(new InetSocketAddress(serverNode.getHost(), serverNode.getPort()));
                } catch (Exception e) {
                    // 连接失败回置状态
                    setChannelStatus(INIT);
                }
            });
            setChannelStatus(CONNECTING);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void connectFailed(SelectionKey selectionKey, Throwable t) {
        ServerNode node = getServerNode();
        logger.error("connect[{}:{}] failed: \"{}\"", node.getHost(), node.getPort(), t.getMessage());
        setChannelStatus(INIT);
    }

    @Override
    public void onServerConnected(SelectionKey selectionKey) {
        setChannelStatus(CONNECTED);
        ServerNode node = getServerNode();
        resetLastConnectTime();
        logger.info("server[{}:{}] is connected", node.getHost(), node.getPort());
        callbackExecutor.submit(() -> channelListener.afterCreated(this));
    }

    @Override
    public ServerNode getServerNode() {
        return serverNode;
    }

    @Override
    public void processChannelData(SelectionKey selectionKey, byte[] data, int transferSize) {
        try {
            RpcResponse response = Serializer.deserialize(data);
            if (!signals.containsKey(response.getRequestId())) {
                // 响应超时
                logger.error("response timeout! requestId[{}]", response.getRequestId());
            } else {
                RpcRequest request = signals.get(response.getRequestId());
                request.setResponse(response);
                request.getSemaphore().release();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public void disconnected(SelectionKey selectionKey) {
        try {
            lock.lock();
            close(socketChannel);
            socketChannel = null;
            this.selectionKey.cancel();
            clearSignals();
            setChannelStatus(INIT);
        } finally {
            lock.unlock();
        }
    }

    private void clearSignals() {
        signals.forEach((requestId, request) -> {
            RpcResponse<Object> response = new RpcResponse<>();
            response.setSuccess(false);
            request.setRequestId(requestId);
            response.setMsg("request[" + requestId + "] failed for channel is closed!");
            response.setCode(ResponseCode.CHANNEL_CLOSED);
            request.setResponse(response);
            request.getSemaphore().release();
        });
        signals.clear();
    }

    @Override
    public void close() {
        try {
            lock.lock();
            if (null != socketChannel) {
                close(socketChannel);
                socketChannel = null;
            }
            if (null != selectionKey) {
                selectionKey.cancel();
            }
            clearSignals();
            setChannelStatus(CLOSED);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SelectorResetListener getSelectorResetListener() {
        return (oldKey, newKey) -> this.selectionKey = newKey;
    }

    @Override
    protected ExecutorService getWorkerExecutor() {
        return workerExecutor;
    }

    @Override
    protected ExecutorService getBossExecutor() {
        return bossExecutor;
    }

    @Override
    public void keepAlive() {
        if (System.currentTimeMillis() - lastKeepAliveTime < keepAliveIntervalSeconds * 1000L) {
            return;
        }
        lastKeepAliveTime = System.currentTimeMillis();
        keepAlive.keepAlive();
    }

    /**
     * 请求内存泄露检查
     */
    public void doLeakedRequestsChecking() {
        long now = System.currentTimeMillis();
        if (now - lastLeakCheckingTime < 10 * 1000) {
            // 10 秒检查一次
            return;
        }
        signals.forEach((id, request) -> {
            if (now - request.getRequestTime() < 2L * request.getTimeoutSeconds() * 1000) {
                return;
            }
            logger.info("found leaked request! id: {}, name: {}.{}", request.getRequestId(), request.getRequest().getInterfaceClz().getName(), request.getRequest().getMethodName());
            signals.remove(id);
        });
        lastLeakCheckingTime = System.currentTimeMillis();
    }

    public boolean isPrepared() {
        return getChannelStatus().isConnected();
    }

    protected void resetLastConnectTime() {
        this.lastConnectTime = 0;
    }

    public void refreshLastConnectTime() {
        lastConnectTime = System.currentTimeMillis();
    }

}
