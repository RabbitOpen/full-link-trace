package rabbit.flt.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rabbit.flt.test.cases.BaseCases;
import rabbit.flt.test.common.SpringBootEntry;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootEntry.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SpringCloudTest extends BaseCases {

    @Test
    public void springBootTest() throws Exception {
        mybatisPlusTest();
        pushStackTest();
        longTraceTest();
        httpClientTest();
    }

}
