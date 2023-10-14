package rabbit.flt.rpc.client.pool;

import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.SelectorResetListener;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.nio.AbstractClientChannel;
import rabbit.flt.rpc.common.rpc.KeepAlive;
import rabbit.flt.rpc.common.rpc.RpcRequest;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

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

    private long lastKeepAliveTime = 0L;

    /**
     * 上次连接时间，重连时使用，防止频繁重试
     */
    private long lastConnectTime = 0L;

    @Override
    public <T> T doRequest(RpcRequest request, int timeoutSeconds) throws RpcException {
        return null;
    }

    @Override
    public void doConnect() {

    }

    @Override
    public void connectFailed(SelectionKey selectionKey, Throwable t) {

    }

    @Override
    public void onServerConnected(SelectionKey selectionKey) {

    }

    @Override
    protected ServerNode getServerNode() {
        return null;
    }

    @Override
    public void processChannelData(SelectionKey selectionKey, byte[] data, int transferSize) {

    }

    @Override
    public void disconnected(SelectionKey selectionKey) {

    }

    @Override
    public void close() {

    }

    @Override
    public SelectorResetListener getSelectorResetListener() {
        return null;
    }

    @Override
    protected ExecutorService getWorkerExecutor() {
        return null;
    }

    @Override
    protected ExecutorService getBossExecutor() {
        return null;
    }

    @Override
    public void keepAlive() {

    }
}
