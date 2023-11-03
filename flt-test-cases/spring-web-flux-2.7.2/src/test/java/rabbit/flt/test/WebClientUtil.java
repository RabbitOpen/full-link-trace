package rabbit.flt.test;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;
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
}
