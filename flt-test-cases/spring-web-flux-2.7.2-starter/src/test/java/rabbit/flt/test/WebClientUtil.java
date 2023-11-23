package rabbit.flt.test;

import junit.framework.TestCase;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;
import rabbit.flt.common.context.TraceContext;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;

public class WebClientUtil {

    private WebClient webClient;

    private ReactorResourceFactory resourceFactory = new ReactorResourceFactory();

    public void init() {
        resourceFactory.setLoopResources(LoopResources.create("webclient-pool", 4, 8, true));
        resourceFactory.setConnectionProvider(ConnectionProvider.builder("httpClient")
                .maxConnections(100)
                .pendingAcquireMaxCount(100)
                .maxLifeTime(Duration.ofMinutes(30))
                .maxIdleTime(Duration.ofSeconds(30))
                .build());
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(resourceFactory, httpClient -> httpClient);
        webClient = WebClient.builder().clientConnector(connector).build();
    }

    public void close() {
        resourceFactory.getConnectionProvider().dispose();
        resourceFactory.destroy();
    }

    public WebClient getWebClient() {
        return webClient;
    }


    protected void monoDeferCase() {
        Mono.defer(() -> {
            TestCase.assertFalse(TraceContext.isTraceOpened());
            return Mono.just(1);
        }).subscribe();
        Mono.fromCallable(() -> {
            TestCase.assertFalse(TraceContext.isTraceOpened());
            return Mono.just(1);
        }).subscribe();
        Mono.fromRunnable(() -> TestCase.assertFalse(TraceContext.isTraceOpened())).subscribe();
        TraceContext.openTrace("defer");
        TraceContext.initRootSpanId();
        Mono.defer(() -> {
            TestCase.assertTrue(TraceContext.isTraceOpened());
            return Mono.just(1);
        }).subscribe();
        Mono.fromCallable(() -> {
            TestCase.assertTrue(TraceContext.isTraceOpened());
            return Mono.just(1);
        }).subscribe();
        Mono.fromRunnable(() -> TestCase.assertTrue(TraceContext.isTraceOpened())).subscribe();
    }
}
