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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootEntry.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SpringCloudStarterTest extends BaseCases {

    @Test
    public void springCloudStarterTest() throws Exception {
        mybatisPlusTest();
        pushStackTest();
        longTraceTest();
        httpClientTest();
        unhandledHttpErrorTest();
        TestCase.assertFalse(ClassProxyLogger.getClassMap().containsKey(IgnoreService.class.getName()));
        TestCase.assertFalse(ClassProxyLogger.getClassMap().containsKey(IgnorePackage.class.getName()));
    }

}
