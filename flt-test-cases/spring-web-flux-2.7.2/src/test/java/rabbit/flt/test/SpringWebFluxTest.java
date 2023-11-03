package rabbit.flt.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.test.cases.BaseCases;
import rabbit.flt.test.common.SpringBootEntry;
import rabbit.flt.test.common.ignore.IgnorePackage;
import rabbit.flt.test.common.service.IgnoreService;
import rabbit.flt.test.starter.ClassProxyLogger;
import reactor.core.publisher.Mono;

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
        webClientCase.cascadedTest(util);
        util.close();
        TestCase.assertFalse(ClassProxyLogger.getClassMap().containsKey(IgnoreService.class.getName()));
        TestCase.assertFalse(ClassProxyLogger.getClassMap().containsKey(IgnorePackage.class.getName()));
        monoDeferCase();
    }

    private void monoDeferCase() {
        Mono.defer(() -> {
            TestCase.assertFalse(TraceContext.isTraceOpened());
            return Mono.just(1);
        }).subscribe();
        TraceContext.openTrace("defer");
        TraceContext.initRootSpanId();
        Mono.defer(() -> {
            TestCase.assertTrue(TraceContext.isTraceOpened());
            return Mono.just(1);
        }).subscribe();
    }


}
