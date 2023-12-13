package rabbit.flt.test;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import rabbit.flt.common.Traceable;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.test.common.spi.TestTraceHandler;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class WebClientCase {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 单次请求
     * @throws Exception
     */
    public void singleHttpRequestTest(WebClientUtil util) throws Exception {
        TestCase.assertFalse(TraceContext.isTraceOpened());
        Map<String, TraceData> map = new ConcurrentHashMap<>();
        Semaphore semaphore = new Semaphore(0);
        TestTraceHandler.setDiscardDataHandler(d -> {
            logger.info("traceData: {}#{}", d.getNodeName(), d.getSpanId());
            map.put(d.getSpanId(), d);
            semaphore.release();
        });
        singleTraceableCall(util);
        semaphore.acquire(5);
        TestCase.assertNotNull(map.get("0"));
        TestCase.assertEquals("singleTraceableCall", map.get("0").getNodeName());
        TestCase.assertNotNull(map.get("0-0"));
        TestCase.assertEquals("WebClient", map.get("0-0").getNodeName());
        TestCase.assertEquals("simpleTrace", map.get("0-1").getNodeName());
        TestCase.assertTrue(map.get("0").getTraceId().equals(map.get("0-0").getTraceId()));
        TestCase.assertTrue(map.get("0-0-0-0").getNodeName().equals("hello"));
        TestCase.assertTrue(map.get("0-0-0").getNodeName().equals("/mvc/hello"));
        TestCase.assertFalse(TraceContext.isTraceOpened());
        TestTraceHandler.setDiscardDataHandler(null);
    }

    @Traceable
    private void singleTraceableCall(WebClientUtil util) throws Exception {
        Semaphore semaphore = new Semaphore(0);
        WebClient.RequestHeadersUriSpec<?> spec = util.getWebClient().get();
        spec.uri("http://localhost:8888/mvc/hello").retrieve().bodyToMono(String.class)
                .subscribe(s -> {
                    logger.info("response: {}", s);
                    semaphore.release();
                });
        simpleTrace();
        semaphore.acquire();
    }

    @Traceable
    private void simpleTrace() {

    }

    /**
     * 级联测试
     * @param util
     * @throws Exception
     */
    public void cascadedTest(WebClientUtil util) throws Exception {
        TestCase.assertFalse(TraceContext.isTraceOpened());
        Map<String, TraceData> map = new ConcurrentHashMap<>();
        Semaphore semaphore = new Semaphore(0);
        TestTraceHandler.setDiscardDataHandler(d -> {
            logger.info("traceData: {}#{}", d.getNodeName(), d.getSpanId());
            map.put(d.getSpanId(), d);
            semaphore.release();
        });
        callHttpContinuously(util);
        semaphore.acquire(7);
        TestCase.assertEquals("callHttpContinuously", map.get("0").getNodeName());
        TestCase.assertEquals("WebClient", map.get("0-0").getNodeName());
        TestCase.assertEquals("/mvc/hello", map.get("0-0-0").getNodeName());
        TestCase.assertEquals("hello", map.get("0-0-0-0").getNodeName());
        TestCase.assertEquals("/mvc/hello1", map.get("0-1-0").getNodeName());
        TestCase.assertEquals("hello1", map.get("0-1-0-0").getNodeName());
        TestCase.assertEquals("WebClient", map.get("0-1").getNodeName());
        TestCase.assertFalse(TraceContext.isTraceOpened());
        TestTraceHandler.setDiscardDataHandler(null);
    }

    @Traceable
    private void callHttpContinuously(WebClientUtil util) {
        Mono<String> fm = util.getWebClient().get().uri("http://localhost:8888/mvc/hello")
                .retrieve().bodyToMono(String.class)
                .flatMap(f -> util.getWebClient().get().uri("http://localhost:8888/mvc/hello1")
                        .retrieve().bodyToMono(String.class));
        TestCase.assertEquals("abc", fm.block());
    }

    public void unHandledError(WebClientUtil util) throws InterruptedException {
        TestCase.assertFalse(TraceContext.isTraceOpened());
        Map<String, TraceData> map = new ConcurrentHashMap<>();
        Semaphore semaphore = new Semaphore(0);
        TestTraceHandler.setDiscardDataHandler(d -> {
            logger.info("traceData: {}#{}", d.getNodeName(), d.getSpanId());
            map.put(d.getSpanId(), d);
            semaphore.release();
        });
        String result = unHandledErrorCase(util);
        semaphore.acquire(4);
        TestCase.assertTrue("error".equals(result));
        TestCase.assertEquals("unHandledErrorCase", map.get("0").getNodeName());
        TestCase.assertEquals("WebClient", map.get("0-0").getNodeName());
        TestCase.assertTrue( map.get("0-0").getHttpResponse().getBody().contains("500"));
        TestCase.assertEquals("/mvc/unHandledError", map.get("0-0-0").getNodeName());
        TestCase.assertTrue(map.get("0-0").getHttpResponse().getBody().contains("500"));
        TestCase.assertEquals("unHandledError", map.get("0-0-0-0").getNodeName());

        TestTraceHandler.setDiscardDataHandler(null);
    }

    @Traceable
    private String unHandledErrorCase(WebClientUtil util) {
        return util.getWebClient().get().uri("http://localhost:8888/mvc/unHandledError")
                .exchangeToMono(r -> Mono.just("error")).block();
    }

    /**
     * 未处理的异常
     *
     * @param util
     * @throws InterruptedException
     */
    public void offlineTest(WebClientUtil util) throws InterruptedException {
        TestCase.assertFalse(TraceContext.isTraceOpened());
        Map<String, TraceData> map = new ConcurrentHashMap<>();
        Semaphore semaphore = new Semaphore(0);
        TestTraceHandler.setDiscardDataHandler(d -> {
            logger.info("traceData: {}#{}", d.getNodeName(), d.getSpanId());
            map.put(d.getSpanId(), d);
            semaphore.release();
        });
        String result = offlineCase(util);
        semaphore.acquire(2);
        TestCase.assertTrue(result.contains("no further information"));
        TestCase.assertEquals("offlineCase", map.get("0").getNodeName());
        TestCase.assertEquals("WebClient", map.get("0-0").getNodeName());
        TestCase.assertTrue(map.get("0-0").getHttpResponse().getBody().contains("no further information"));
        TestTraceHandler.setDiscardDataHandler(null);
    }

    /**
     * 请求不在的服务
     *
     * @param util
     * @return
     */
    @Traceable
    private String offlineCase(WebClientUtil util) {
        try {
            return util.getWebClient().get().uri("http://localhost:12898/mvc/unHandledError")
                    .exchangeToMono(r -> Mono.just("")).block();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
