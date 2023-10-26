package rabbit.flt.rpc.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.common.Metrics;
import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.client.RequestFactory;
import rabbit.flt.rpc.client.pool.ChannelResourcePool;
import rabbit.flt.rpc.client.pool.ConfigBuilder;
import rabbit.flt.rpc.common.NamedExecutor;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.rpc.ProtocolService;
import rabbit.flt.rpc.server.Server;
import rabbit.flt.rpc.server.ServerBuilder;
import reactor.core.publisher.Mono;

import java.net.StandardSocketOptions;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

@RunWith(JUnit4.class)
public class MonoRpcTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void monoRpcTest() throws Exception {
        int port = 10012;
        String host = "localhost";
        Server server = ServerBuilder.builder()
                .workerExecutor(NamedExecutor.fixedThreadsPool(1, "worker-executor-"))
                .bossExecutor(NamedExecutor.fixedThreadsPool(1, "boss-executor-"))
                .host(host).port(port)
                .socketOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024)
                .socketOption(StandardSocketOptions.SO_REUSEADDR, true)
                .maxFrameLength(16 * 1024 * 1024)
                .maxPendingConnections(1000)
                .build();
        server.getRequestDispatcher().registerDirectly(MonoUserService.class, new MonoUserService() {
            @Override
            public Mono<String> getUserName() {
                return Mono.just("abc");
            }
        }).registerDirectly(ProtocolService.class, new ProtocolService() {
            @Override
            public List<ServerNode> getServerNodes() {
                return Arrays.asList(new ServerNode(host, port));
            }

            @Override
            public boolean isMetricsEnabled(String applicationCode, Class<? extends Metrics> type) {
                return false;
            }
        });
        server.start();
        ChannelResourcePool resourcePool = new ChannelResourcePool() {
        };
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
        MonoUserService userService = requestFactory.proxy(MonoUserService.class);
        Semaphore semaphore = new Semaphore(0);
        userService.getUserName().subscribe(s -> {
            logger.info("response: {}", s);
            semaphore.release();
        });
        logger.info("finish request");
        semaphore.acquire();
        server.close();
        resourcePool.close();
    }
}
