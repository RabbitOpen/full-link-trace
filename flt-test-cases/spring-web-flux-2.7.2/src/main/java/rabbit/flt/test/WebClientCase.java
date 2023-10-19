package rabbit.flt.test;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import rabbit.flt.common.Traceable;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.test.common.spi.TestTraceHandler;

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
}
