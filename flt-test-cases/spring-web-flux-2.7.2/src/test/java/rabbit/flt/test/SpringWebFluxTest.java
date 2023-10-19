package rabbit.flt.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rabbit.flt.test.cases.BaseCases;
import rabbit.flt.test.common.SpringBootEntry;

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
    }


 }