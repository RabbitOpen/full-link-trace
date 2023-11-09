package rabbit.flt.rpc.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.Metrics;
import rabbit.flt.common.metrics.*;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.utils.ReflectUtils;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.client.RequestFactory;
import rabbit.flt.rpc.client.handler.RpcMetricsDataHandler;
import rabbit.flt.rpc.client.handler.RpcTraceDataHandler;
import rabbit.flt.rpc.client.pool.*;
import rabbit.flt.rpc.common.NamedExecutor;
import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.exception.*;
import rabbit.flt.rpc.common.nio.ChannelProcessor;
import rabbit.flt.rpc.common.nio.SelectorWrapper;
import rabbit.flt.rpc.common.rpc.Authentication;
import rabbit.flt.rpc.common.rpc.DataService;
import rabbit.flt.rpc.common.rpc.KeepAlive;
import rabbit.flt.rpc.common.rpc.ProtocolService;
import rabbit.flt.rpc.server.ClientEventHandler;
import rabbit.flt.rpc.server.Server;
import rabbit.flt.rpc.server.ServerBuilder;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

@RunWith(JUnit4.class)
public class RpcTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 连接失败测试
     *
     * @throws Exception
     */
    @Test
    public void connectFailedTest() throws Exception {
        Semaphore semaphore = new Semaphore(0);
        ResourceGuard guard = new ResourceGuard();
        ServerNode serverNode = new ServerNode("localhost", 21390);
        SelectorWrapper wrapper = new SelectorWrapper();
        ClientChannel channel = new ClientChannel(null, null, serverNode, guard, wrapper) {
            @Override
            public void connectFailed(SelectionKey selectionKey, Throwable t) {
                super.connectFailed(selectionKey, t);
                resetLastConnectTime();
                semaphore.release();
                guard.wakeup();
            }
        };
        ChannelProcessor processor = new ChannelProcessor(wrapper, channel);
        processor.setDaemon(false);
        processor.start();
        semaphore.acquire(2);
        guard.close();
        processor.close();
        channel.close();
        wrapper.close();
    }

    /**
     * rpc调用
     *
     * @throws Exception
     */
    @Test
    public void simpleRpcTest() throws Exception {
        int port = 10001;
        String host = "localhost";
        Server server = ServerBuilder.builder()
                .workerExecutor(NamedExecutor.fixedThreadsPool(1, "worker-executor-"))
                .host(host).port(port)
                .socketOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024)
                .socketOption(StandardSocketOptions.SO_REUSEADDR, true)
                .registerHandler(Authentication.class, (app, sig) -> {
                    if (StringUtils.isEmpty(sig)) {
                        throw new RuntimeException();
                    }
                })
                .registerHandler(UserService.class, name -> name + "001")
                .maxFrameLength(16 * 1024 * 1024)
                .maxIdleSeconds(300)
                .clientEventHandler(new ClientEventHandler() {
                })
                .maxPendingConnections(1000)
                .build();

        server.start();
        Semaphore serverClosedSemaphore = new Semaphore(0);
        Semaphore serverConnectedSemaphore = new Semaphore(0);
        ChannelResourcePool resourcePool = new ChannelResourcePool() {
            @Override
            public void disconnected(SelectionKey selectionKey) {
                super.disconnected(selectionKey);
                serverClosedSemaphore.release();
            }

            @Override
            public void onServerConnected(SelectionKey selectionKey) {
                super.onServerConnected(selectionKey);
                serverConnectedSemaphore.release();
            }
        };

        RequestFactory requestFactory = new RequestFactory() {
            @Override
            protected Client getClient() {
                return resourcePool;
            }

            @Override
            protected int getMaxRetryTime() {
                return 0;
            }

            @Override
            protected int getRequestTimeoutSeconds() {
                return 2;
            }
        };

        resourcePool.init(ConfigBuilder.builder()
                .workerThreadCount(1)
                .bossThreadCount(1)
                .password("1234567f1234567f")
                .connectionsPerServer(1)
                .maxRetryTime(0)
                .acquireClientTimeoutSeconds(3)
                .keepAliveIntervalSeconds(10)
                .rpcRequestTimeoutSeconds(2)
                .serverNodes(Arrays.asList(new ServerNode(host, port), new ServerNode(host, port + 4)))
                .build());

        KeepAlive keepAlive = requestFactory.proxy(KeepAlive.class);
        keepAlive.keepAlive();

        Authentication authService = requestFactory.proxy(Authentication.class);
        UserService userService = requestFactory.proxy(UserService.class);
        try {
            userService.getName("hello");
            throw new RuntimeException();
        } catch (Exception e) {
            TestCase.assertEquals(AuthenticationException.class, e.getClass());
        }
        // 密码为空认证会失败
        try {
            authService.authenticate("", "");
            throw new RuntimeException();
        } catch (AuthenticationException e) {
            logger.error(e.getMessage());
        }
        String password = "123";

        authService.authenticate("", password);
        TestCase.assertEquals("hello001", userService.getName("hello"));

        String msg = "error call";
        try {
            userService.exceptionCall(msg);
        } catch (RpcException e) {
            TestCase.assertEquals(msg, e.getMessage());
        }

        try {
            // 验证未注册的接口调用
            DataService dataService = requestFactory.proxy(DataService.class);
            dataService.handleGcMetrics(new GcMetrics());
            throw new RuntimeException();
        } catch (Exception e) {
            TestCase.assertTrue(e instanceof UnRegisteredHandlerException);
        }

        serverConnectedSemaphore.drainPermits();
        try {
            // 超时
            userService.wait5s();
            throw new RuntimeException();
        } catch (Exception e) {
            TestCase.assertEquals(RpcTimeoutException.class, e.getClass());
        }
        serverConnectedSemaphore.acquire(1);
        server.close();
        serverClosedSemaphore.acquire(1);
        try {
            authService.authenticate("", password);
            throw new RuntimeException();
        } catch (Exception e) {
            TestCase.assertTrue(e instanceof NoPreparedClientException);
        }
        resourcePool.close();
    }

    /**
     * 刷新服务端节点列表测试
     *
     * @throws Exception
     */
    @Test
    public void refreshServerTest() throws Exception {
        int port = 10001;
        String host = "localhost";
        Semaphore refreshServerSemaphore = new Semaphore(0);
        ChannelResourcePool resourcePool = new SecureChannelResourcePool() {
            @Override
            public void refreshServerNodes(List<ServerNode> nodeList) {
                super.refreshServerNodes(nodeList);
                refreshServerSemaphore.release();
            }
        };
        Server server = ServerBuilder.builder()
                .workerThreadCount(1)
                .host(host).port(port)
                .socketOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024)
                .socketOption(StandardSocketOptions.SO_REUSEADDR, true)
                .registerHandler(Authentication.class, (app, sig) -> {
                })
                .registerHandler(ProtocolService.class, () -> Arrays.asList(new ServerNode(host, port),
                        new ServerNode(host, port),
                        new ServerNode(host, port + 1),
                        new ServerNode(host, port + 2)
                ))
                .maxFrameLength(16 * 1024 * 1024)
                .maxPendingConnections(1000)
                .build();

        server.start();

        // 每个服务端2个连接
        int connectionsPerServer = 2;
        resourcePool.init(ConfigBuilder.builder()
                .workerThreadCount(1)
                .bossThreadCount(1)
                .password("1234567f1234567f")
                .connectionsPerServer(connectionsPerServer)
                .acquireClientTimeoutSeconds(3)
                .serverNodes(Arrays.asList(new ServerNode(host, port), new ServerNode(host, port + 10)))
                .build());

        ResourceGuard resourceGuard = resourcePool.getResourceGuard();
        refreshServerSemaphore.acquire(1);
        int size = resourcePool.getPoolConfig().getServerNodes().size();
        TestCase.assertEquals(connectionsPerServer * size, resourceGuard.getClientChannels().size());
        TestCase.assertEquals(connectionsPerServer * size, resourcePool.getClientChannelList().size());
        int connected = 0, unconnected = 0;
        for (ClientChannel channel : resourcePool.getClientChannelList()) {
            if (channel.getServerNode().isSameNode(new ServerNode(host, port))) {
                connected++;
            } else {
                unconnected++;
            }
        }
        TestCase.assertEquals(4, unconnected);
        TestCase.assertEquals(2, connected);
        resourcePool.close();
        server.close();
    }

    /**
     * epoll bug 自修复测试
     *
     * @throws Exception
     */
    @Test
    public void epollBugTest() throws Exception {
        logger.info("-------------------------  begin epollBugTest  -------------------------");
        int port = 10035;
        String host = "localhost";
        ChannelResourcePool resourcePool = new SecureChannelResourcePool();
        Server server = ServerBuilder.builder()
                .workerThreadCount(8)
                .bossThreadCount(2)
                .host(host).port(port)
                .socketOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024)
                .socketOption(StandardSocketOptions.SO_REUSEADDR, true)
                .registerHandler(Authentication.class, (app, sig) -> resourcePool.getResourceGuard().wakeup())
                .registerHandler(ProtocolService.class, () -> Arrays.asList(new ServerNode(host, port),
                        new ServerNode(host, port + 1),
                        new ServerNode(host, port + 2)
                ))
                .registerHandler(UserService.class, name -> name + "001")
                .maxFrameLength(16 * 1024 * 1024)
                .maxPendingConnections(1000)
                .build();

        server.start();

        RequestFactory requestFactory = new RequestFactory() {
            @Override
            protected Client getClient() {
                return resourcePool;
            }

            @Override
            protected int getMaxRetryTime() {
                return 0;
            }

            @Override
            protected int getRequestTimeoutSeconds() {
                return 2;
            }
        };

        // 每个服务端2个连接
        int connectionsPerServer = 2;
        resourcePool.init(ConfigBuilder.builder()
                .workerThreadCount(1)
                .bossThreadCount(1)
                .password("1234567f1234567f")
                .connectionsPerServer(connectionsPerServer)
                .acquireClientTimeoutSeconds(3)
                .serverNodes(Arrays.asList(new ServerNode(host, port), new ServerNode(host, port + 10)))
                .build());

        UserService userService = requestFactory.proxy(UserService.class);
        Semaphore requestCounter = new Semaphore(0);
        long start = System.currentTimeMillis();
        int times = 1000;
        int threads = 10;
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                for (int j = 0; j < times; j++) {
                    if (100 == j) {
                        resourcePool.getWrapper().addHookJob(() -> {
                            try {
                                Field processorField = ChannelResourcePool.class.getDeclaredField("channelProcessor");
                                Object processor = ReflectUtils.getValue(resourcePool, processorField);
                                Method method = processor.getClass().getDeclaredMethod("rebuildSelectorWhenEpollBugFound");
                                method.setAccessible(true);
                                method.invoke(processor);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        });
                    }
                    try {
                        // 发送完数据还未等到响应，selector挂了，此时可能出现异常
                        TestCase.assertEquals("name" + j + "001", userService.getName("name" + j));
                    } catch (ChannelClosedException e) {
                        logger.warn(e.getMessage());
                    }
                    requestCounter.release();
                }
            }).start();
        }
        requestCounter.acquire(times * threads);
        logger.info("cost: {}", System.currentTimeMillis() - start);
        resourcePool.close();
        server.close();
    }

    /**
     * 服务重连测试
     *
     * @throws Exception
     */
    @Test
    public void reconnectTest() throws Exception {
        int port = 10001;
        String host = "localhost";
        Semaphore connectSemaphore = new Semaphore(0);
        ChannelResourcePool resourcePool = new SecureChannelResourcePool() {
            @Override
            public void onServerConnected(SelectionKey selectionKey) {
                super.onServerConnected(selectionKey);
                connectSemaphore.release();
            }
        };

        Server server = ServerBuilder.builder()
                .workerThreadCount(2)
                .bossThreadCount(1)
                .host(host).port(port)
                .socketOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024)
                .socketOption(StandardSocketOptions.SO_REUSEADDR, true)
                .registerHandler(Authentication.class, (app, sig) -> resourcePool.getResourceGuard().wakeup())
                .registerHandler(ProtocolService.class, () -> Arrays.asList(new ServerNode(host, port)))
                .registerHandler(UserService.class, name -> name + "001")
                .maxFrameLength(16 * 1024 * 1024)
                .maxPendingConnections(1000)
                .build();

        server.start();

        RequestFactory requestFactory = new RequestFactory() {
            @Override
            protected Client getClient() {
                return resourcePool;
            }

            @Override
            protected int getMaxRetryTime() {
                return 0;
            }

            @Override
            protected int getRequestTimeoutSeconds() {
                return 2;
            }
        };

        // 每个服务端1个连接
        int connectionsPerServer = 5;
        resourcePool.init(ConfigBuilder.builder()
                .workerThreadCount(2)
                .bossThreadCount(1)
                .password("1234567f1234567f")
                .connectionsPerServer(connectionsPerServer)
                .acquireClientTimeoutSeconds(3)
                .rpcRequestTimeoutSeconds(2)
                .serverNodes(Arrays.asList(new ServerNode(host, port)))
                .build());

        UserService userService = requestFactory.proxy(UserService.class);
        userService.getName("abc");
        server.close();
        try {
            userService.getName("abc");
            throw new RuntimeException();
        } catch (Exception e) {
            if (!(e instanceof RpcException)) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        }

        waitAllConnectionClosed(resourcePool, connectionsPerServer);

        connectSemaphore.drainPermits();
        for (int i = 0; i < 5; i++) {
            logger.info("\n--------------------        restart server       -----------------------");
            server.start();
            connectSemaphore.acquire(connectionsPerServer);
            TestCase.assertEquals("abc001", userService.getName("abc"));
            server.close();
            waitAllConnectionClosed(resourcePool, connectionsPerServer);
        }
        resourcePool.close();
    }

    private void waitAllConnectionClosed(ChannelResourcePool resourcePool, int connectionsPerServer) {
        while (true) {
            int count = 0;
            for (ClientChannel channel : resourcePool.getClientChannelList()) {
                if (channel.getChannelStatus().isInit()) {
                    count++;
                }
            }
            if (count == connectionsPerServer) {
                break;
            } else {
                LockSupport.parkNanos(10L * 1000 * 1000);
            }
        }
    }


    @Test
    public void agentRequestFactoryTest() throws Exception {
        int port = 12304;
        Map<String, Object> map = new ConcurrentHashMap<>();
        Semaphore semaphore = new Semaphore(0);
        Server server = ServerBuilder.builder()
                .workerThreadCount(2)
                .bossThreadCount(1)
                .host("localhost").port(port)
                .socketOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024)
                .socketOption(StandardSocketOptions.SO_REUSEADDR, true)
                .registerHandler(Authentication.class, (app, sig) -> {
                })
                .registerHandler(ProtocolService.class, new ProtocolService() {
                    @Override
                    public List<ServerNode> getServerNodes() {
                        return Arrays.asList(new ServerNode("localhost", port));
                    }

                    @Override
                    public boolean isMetricsEnabled(String applicationCode, Class<? extends Metrics> type) {
                        return true;
                    }
                })
                .registerHandler(DataService.class, new DataService() {
                    @Override
                    public void handleTraceData(List<TraceData> list) {
                        map.put("trace", list);
                        semaphore.release();
                    }

                    @Override
                    public void handleGcMetrics(GcMetrics metrics) {

                    }

                    @Override
                    public void handleMemoryMetrics(MemoryMetrics metrics) {
                        map.put("metrics", metrics);
                        semaphore.release();
                    }

                    @Override
                    public boolean handleEnvironmentMetrics(EnvironmentMetrics metrics) {
                        map.put("metrics", metrics);
                        semaphore.release();
                        return true;
                    }

                    @Override
                    public void handleDiskSpaceMetrics(DiskSpaceMetrics metrics) {

                    }

                    @Override
                    public void handleDiskIoMetrics(DiskIoMetrics metrics) {

                    }

                    @Override
                    public void handleCpuMetrics(CpuMetrics metrics) {

                    }

                    @Override
                    public void handleNetworkMetrics(NetworkMetrics metrics) {

                    }
                })
                .registerHandler(UserService.class, name -> name + "001")
                .maxFrameLength(16 * 1024 * 1024)
                .maxPendingConnections(1000)
                .build();

        server.start();
        AbstractConfigFactory.setFactoryLoader(() -> new AbstractConfigFactory() {
            @Override
            public void initialize() {

            }

            @Override
            protected AgentConfig getAgentConfig() {
                AgentConfig config = new AgentConfig();
                config.setServers("localhost:12304");
                config.setRpcRequestTimeoutSeconds(2);
                config.setApplicationCode("testApp");
                config.setSecurityKey("testApp1testApp1");
                return config;
            }
        });
        RpcMetricsDataHandler metricsDataHandler = new RpcMetricsDataHandler();
        TestCase.assertTrue(metricsDataHandler.handle(new EnvironmentMetrics()));
        TestCase.assertTrue(metricsDataHandler.isMetricsEnabled(EnvironmentMetrics.class));
        semaphore.acquire();
        TestCase.assertTrue(map.get("metrics") instanceof EnvironmentMetrics);
        MemoryMetrics memoryMetrics = new MemoryMetrics();
        metricsDataHandler.handle(memoryMetrics);
        semaphore.acquire();
        TestCase.assertTrue(map.get("metrics") instanceof MemoryMetrics);
        RpcTraceDataHandler traceDataHandler = new RpcTraceDataHandler();
        traceDataHandler.process(Arrays.asList(new TraceData()));
        semaphore.acquire();
        TestCase.assertTrue(map.get("trace") instanceof List);
        server.close();
    }

    @Test
    public void monoTest() {
        String value = "world";
        Object block = Mono.create(m -> m.success(m.contextView().get("hello")))
                .onErrorResume(e -> Mono.empty()).contextWrite(ctx -> ctx.put("hello", value))
                .map(d -> {
                    logger.info("context: {}", d);
                    return d;
                }).block();
        TestCase.assertEquals(value, block);
    }
}
