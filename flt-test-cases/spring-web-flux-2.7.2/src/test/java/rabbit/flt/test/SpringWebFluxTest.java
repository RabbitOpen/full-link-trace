package rabbit.flt.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import rabbit.flt.common.Traceable;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.test.cases.BaseCases;
import rabbit.flt.test.common.SpringBootEntry;
import rabbit.flt.test.common.spi.TestTraceHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootEntry.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SpringWebFluxTest extends BaseCases {

    private WebClientUtil util = new WebClientUtil();

    @Test
    public void webFluxTest() throws Exception {
        util.init();
        mybatisPlusTest();
        contextStatusTest();
        pushStackTest();
        longTraceTest();
        httpClientTest();
        WebClientCase webClientCase = new WebClientCase();
        webClientCase.singleHttpRequestTest(util);
        util.close();
    }


 }
