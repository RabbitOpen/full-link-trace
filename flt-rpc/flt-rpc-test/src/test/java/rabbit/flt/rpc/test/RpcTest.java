package rabbit.flt.rpc.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.common.Metrics;
import rabbit.flt.common.metrics.GcMetrics;
import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.client.RequestFactory;
import rabbit.flt.rpc.client.pool.ChannelResourcePool;
import rabbit.flt.rpc.client.pool.ClientChannel;
import rabbit.flt.rpc.client.pool.ConfigBuilder;
import rabbit.flt.rpc.client.pool.ResourceGuard;
import rabbit.flt.rpc.client.pool.SecureChannelResourcePool;
import rabbit.flt.rpc.common.NamedExecutor;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.exception.NoPreparedClientException;
import rabbit.flt.rpc.common.exception.UnAuthenticatedException;
import rabbit.flt.rpc.common.exception.UnRegisteredHandlerException;
import rabbit.flt.rpc.common.nio.ChannelProcessor;
import rabbit.flt.rpc.common.nio.SelectorWrapper;
import rabbit.flt.rpc.common.rpc.Authentication;
import rabbit.flt.rpc.common.rpc.DataService;
import rabbit.flt.rpc.common.rpc.KeepAlive;
import rabbit.flt.rpc.common.rpc.ProtocolService;
import rabbit.flt.rpc.server.Server;
import rabbit.flt.rpc.server.ServerBuilder;

import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

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
                .workerExecutor(NamedExecutor.fixedThreadsPool(1, "boss-executor-"))
                .host(host).port(port)
                .socketOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024)
                .registerHandler(Authentication.class, (app, sig) -> true)
                .registerHandler(UserService.class, name -> name + "001")
                .maxPendingConnections(16 * 1024 * 1024)
                .maxPendingConnections(1000)
                .build();

        server.start();
        Semaphore serverClosedSemaphore = new Semaphore(0);
        Semaphore refreshServerSemaphore = new Semaphore(0);
        ChannelResourcePool resourcePool = new ChannelResourcePool() {
            @Override
            public void disconnected(SelectionKey selectionKey) {
                super.disconnected(selectionKey);
                serverClosedSemaphore.release();
            }

            @Override
            public void refreshServerNodes(List<ServerNode> nodeList) {
                super.refreshServerNodes(nodeList);
                refreshServerSemaphore.release();
            }
        };

        RequestFactory requestFactory = new RequestFactory() {
            @Override
            protected Client getClient() {
                return resourcePool;
            }

            @Override
            protected int getMaxRetryTime() {
                return 2;
            }

            @Override
            protected int getRequestTimeoutSeconds() {
                return 30;
            }
        };

        resourcePool.init(ConfigBuilder.builder()
                .workerThreadCount(1)
                .bossThreadCount(1)
                .password("1234567f1234567f")
                .connectionsPerServer(1)
                .acquireClientTimeoutSeconds(3)
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
            TestCase.assertEquals(UnAuthenticatedException.class, e.getClass());
        }
        TestCase.assertTrue(authService.authenticate("", ""));
        TestCase.assertEquals("hello001", userService.getName("hello"));

        try {
            // 验证未注册的接口调用
            DataService dataService = requestFactory.proxy(DataService.class);
            dataService.handleGcMetrics(new GcMetrics());
            throw new RuntimeException();
        } catch (Exception e) {
            TestCase.assertTrue(e instanceof UnRegisteredHandlerException);
        }

        server.close();
        serverClosedSemaphore.acquire(1);

        try {
            authService.authenticate("", "");
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
                .registerHandler(Authentication.class, (app, sig) -> true)
                .registerHandler(ProtocolService.class, new ProtocolService() {
                    @Override
                    public List<ServerNode> getServerNodes() {
                        return Arrays.asList(new ServerNode(host, port),
                                new ServerNode(host, port),
                                new ServerNode(host, port + 1),
                                new ServerNode(host, port + 2)
                        );
                    }

                    @Override
                    public boolean isMetricsEnabled(String applicationCode, Class<? extends Metrics> type) {
                        return false;
                    }
                })
                .maxPendingConnections(16 * 1024 * 1024)
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

}
