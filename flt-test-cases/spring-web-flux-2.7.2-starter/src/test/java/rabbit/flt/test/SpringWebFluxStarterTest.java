package rabbit.flt.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import rabbit.flt.common.utils.VersionUtils;
import rabbit.flt.core.loader.DefaultPluginClassLoader;
import rabbit.flt.test.cases.BaseCases;
import rabbit.flt.test.common.ignore.IgnorePackage;
import rabbit.flt.test.common.service.IgnoreService;
import rabbit.flt.test.starter.ClassProxyLogger;
import rabbit.flt.test.starter.SpringBootStartListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

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
        util.init();
        mybatisPlusTest();
        contextStatusTest();
        pushStackTest();
        longTraceTest();
        httpClientTest();
        WebClientCase webClientCase = new WebClientCase();
        webClientCase.singleHttpRequestTest(util);
        webClientCase.cascadedTest(util);
        webClientCase.errorCallTest(util);
        webClientCase.unHandledError(util);
        util.close();
        TestCase.assertFalse(ClassProxyLogger.getClassMap().containsKey(IgnoreService.class.getName()));
        TestCase.assertFalse(ClassProxyLogger.getClassMap().containsKey(IgnorePackage.class.getName()));
        util.monoDeferCase();

        classLoaderTest();
    }

    private void classLoaderTest() throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        String baseDir = VersionUtils.getProperty("flt.properties", "baseDir");
        URLClassLoader classLoader = new URLClassLoader(new URL[]{
                new File(baseDir + "/../flt-core/target").toURI().toURL(),
        }, ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        String jarPath = baseDir + "/../flt-core/target/flt-core-" + VersionUtils.getVersion() + ".jar";
        DefaultPluginClassLoader pluginClassLoader = new DefaultPluginClassLoader() {
            @Override
            protected String getAgentJarFilePath() {
                return jarPath;
            }
        };
        Class<?> clz = pluginClassLoader.loadClassByName("rabbit.flt.plugins.logback.plugin.LineNumberPlugin");
        TestCase.assertNotNull(clz);
    }


}
