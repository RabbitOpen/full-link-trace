package rabbit.flt.rpc.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.Metrics;
import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.client.RequestFactory;
import rabbit.flt.rpc.client.pool.ConfigBuilder;
import rabbit.flt.rpc.client.pool.SecureChannelResourcePool;
import rabbit.flt.rpc.common.NamedExecutor;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.rpc.Authentication;
import rabbit.flt.rpc.common.rpc.KeepAlive;
import rabbit.flt.rpc.common.rpc.ProtocolService;
import rabbit.flt.rpc.server.Server;
import rabbit.flt.rpc.server.ServerBuilder;

import java.net.StandardSocketOptions;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class AuthenticatedRpcTest {

    @Test
    public void authenticatedRpcTest() {
        int port = 10002;
        String host = "localhost";
        Server server = ServerBuilder.builder()
                .workerExecutor(NamedExecutor.fixedThreadsPool(1, "worker-executor-"))
                .bossExecutor(NamedExecutor.fixedThreadsPool(1, "boss-executor-"))
                .host(host).port(port)
                .socketOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024)
                .socketOption(StandardSocketOptions.SO_REUSEADDR, true)
                .registerHandler(Authentication.class, (app, sig) -> {})
                .registerHandler(UserService.class, name -> name + "001")
                .registerHandler(ProtocolService.class, new ProtocolService() {
                    @Override
                    public List<ServerNode> getServerNodes() {
                        return Arrays.asList(new ServerNode(host, port));
                    }

                    @Override
                    public boolean isMetricsEnabled(String applicationCode, Class<? extends Metrics> type) {
                        return false;
                    }
                })
                .maxFrameLength(16 * 1024 * 1024)
                .maxPendingConnections(1000)
                .build();
        server.start();
        SecureChannelResourcePool resourcePool = new SecureChannelResourcePool();
        RequestFactory requestFactory = new RequestFactory() {
            @Override
            protected Client getClient() {
                return resourcePool;
            }

            @Override
            protected int getMaxRetryTime() {
                return 3;
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
                .serverNodes(Arrays.asList(new ServerNode(host, port)))
                .build());

        KeepAlive keepAlive = requestFactory.proxy(KeepAlive.class);
        keepAlive.keepAlive();
        UserService userService = requestFactory.proxy(UserService.class);
        TestCase.assertEquals("hello001", userService.getName("hello"));
        server.close();
        resourcePool.close();
    }
}
