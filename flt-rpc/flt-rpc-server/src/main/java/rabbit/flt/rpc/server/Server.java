package rabbit.flt.rpc.server;

import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.rpc.common.*;
import rabbit.flt.rpc.common.nio.AbstractServerChannel;
import rabbit.flt.rpc.common.nio.ChannelProcessor;
import rabbit.flt.rpc.common.nio.SelectorWrapper;
import rabbit.flt.rpc.common.rpc.Authentication;
import rabbit.flt.rpc.common.rpc.KeepAlive;
import rabbit.flt.rpc.common.rpc.RpcRequest;
import rabbit.flt.rpc.common.rpc.RpcResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Server extends AbstractServerChannel implements Registrar {

    /**
     * 服务端端口
     */
    private int port;

    private int maxPendingConnections = 1024;

    private ServerSocketChannel serverSocketChannel;

    // 业务线程数
    private int workerThreadCount = 8;

    private int bossThreadCount = 4;

    private String host;

    /**
     * io线程池，不可执行长期阻塞的任务
     */
    private ExecutorService bossExecutor;

    /**
     * 业务线程池
     */
    private ExecutorService workerExecutor;

    private ChannelProcessor processor;

    private ContextManager contextManager;

    private RequestDispatcher requestDispatcher = new RequestDispatcher();

    // socket 参数
    private List<SocketOptionPair> socketOptionPairs = new ArrayList<>();

    private boolean started = false;

    /**
     * 最大空闲时间
     */
    private int maxIdleSeconds = 5 * 60;

    /**
     * 客户端事件处理器
     */
    private ClientEventHandler clientEventHandler = new ClientEventHandler() {
        // do nothing
    };

    protected Server() {
        // 注册认证，默认通过
        register(Authentication.class, (applicationCode, signature) -> {
            // do nothing
        });
        // 注册心跳
        getRequestDispatcher().registerDirectly(KeepAlive.class, (KeepAlive) () -> {
            // do nothing
        });
    }

    /**
     * 启动服务
     *
     * @return
     * @throws IOException
     */
    public synchronized Server start() {
        if (started) {
            throw new RpcException("illegal status error, server is started!");
        }
        try {
            started = true;
            contextManager = new ContextManager(this);
            selectorWrapper = new SelectorWrapper();
            serverSocketChannel = ServerSocketChannel.open();
            for (SocketOptionPair optionPair : socketOptionPairs) {
                serverSocketChannel.setOption(optionPair.getKey(), optionPair.getValue());
            }
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(getHost(), port), maxPendingConnections);
            serverSocketChannel.register(getWrapper().getSelector(), SelectionKey.OP_ACCEPT);
            processor = new ChannelProcessor(selectorWrapper, this);
            processor.start();
            if (null == workerExecutor) {
                workerExecutor = NamedExecutor.fixedThreadsPool(this.workerThreadCount, "worker-executor-");
            }
            if (null == bossExecutor) {
                bossExecutor = NamedExecutor.fixedThreadsPool(this.bossThreadCount, "boss-executor-");
            }
            logger.info("server is started");
            return this;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void close() {
        processor.close();
        bossExecutor.shutdown();
        workerExecutor.shutdown();
        contextManager.close();
        selectorWrapper.close();
        close(serverSocketChannel);
        logger.info("server is closed!");
        bossExecutor = null;
        workerExecutor = null;
        started = false;
    }

    /**
     * 设置网络参数
     *
     * @param key
     * @param value
     */
    public <T> void setSocketOption(SocketOption<T> key, T value) {
        socketOptionPairs.add(new SocketOptionPair(key, value));
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMaxPendingConnections(int maxPendingConnections) {
        this.maxPendingConnections = maxPendingConnections;
    }

    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public void setBossThreadCount(int bossThreadCount) {
        this.bossThreadCount = bossThreadCount;
    }

    protected String getHost() {
        String[] ip = {"0", "0", "0", "0"};
        return StringUtils.isEmpty(host) ? String.join(".", ip) : host;
    }

    public void setBossExecutor(ExecutorService bossExecutor) {
        this.bossExecutor = bossExecutor;
    }

    public void setWorkerExecutor(ExecutorService workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    protected void onClientConnected(SelectionKey selectionKey) {
        getClientEventHandler().onClientConnected(selectionKey);
    }

    @Override
    public void processChannelData(SelectionKey selectionKey, byte[] data, int transferSize) {
        RpcRequest rpcRequest;
        try {
            rpcRequest = Serializer.deserialize(data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            contextManager.closeKey(selectionKey);
            return;
        }
        Request request = rpcRequest.getRequest();
        Object handler = getRequestDispatcher().getHandler(request.getInterfaceClz());
        if (null == handler) {
            logger.error("unregistered handler[{}]", request.getInterfaceClz());
            RpcResponse<Object> response = new RpcResponse<>();
            response.setRequestId(rpcRequest.getRequestId());
            response.setSuccess(false);
            response.setMsg("unregistered handler[" + request.getInterfaceClz().getName() + "]");
            response.setCode(ResponseCode.UN_REGISTERED_HANDLER);
            getRequestDispatcher().write(selectionKey, response);
        } else {
            contextManager.active(selectionKey);
            getRequestDispatcher().handleRequest(selectionKey, rpcRequest);
        }
    }

    @Override
    public void disconnected(SelectionKey selectionKey) {
        contextManager.inActive(selectionKey);
    }

    @Override
    public SelectorResetListener getSelectorResetListener() {
        return (oldKey, newKey) -> {
            contextManager.active(newKey);
            contextManager.inActive(oldKey);
        };
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
    public <T> void register(Class<T> clz, T handler) {
        getRequestDispatcher().register(clz, handler);
    }

    public RequestDispatcher getRequestDispatcher() {
        return requestDispatcher;
    }

    public int getMaxIdleSeconds() {
        return maxIdleSeconds;
    }

    public void setMaxIdleSeconds(int maxIdleSeconds) {
        this.maxIdleSeconds = maxIdleSeconds;
    }

    public ClientEventHandler getClientEventHandler() {
        return clientEventHandler;
    }

    public void setClientEventHandler(ClientEventHandler clientEventHandler) {
        this.clientEventHandler = clientEventHandler;
    }
}
