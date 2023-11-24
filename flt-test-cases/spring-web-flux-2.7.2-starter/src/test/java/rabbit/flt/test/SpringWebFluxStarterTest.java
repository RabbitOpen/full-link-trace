package rabbit.flt.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rabbit.flt.test.cases.BaseCases;
import rabbit.flt.test.common.ignore.IgnorePackage;
import rabbit.flt.test.common.service.IgnoreService;
import rabbit.flt.test.starter.ClassProxyLogger;
import rabbit.flt.test.starter.SpringBootStartListener;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringWebFluxEntry.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SpringWebFluxStarterTest extends BaseCases {

    static {
        new SpringBootStartListener().onApplicationEvent(null);
    }

    private WebClientUtil util = new WebClientUtil();

    @Test
    public void webFluxStarterTest() throws Exception {
        // 通过starter加载， 注意Mono加载时机点
        TestCase.assertTrue(ClassProxyLogger.getClassMap().containsKey("reactor.core.publisher.Mono"));
        httpClientTest();
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
        util.monoDeferCase();
    }




}
