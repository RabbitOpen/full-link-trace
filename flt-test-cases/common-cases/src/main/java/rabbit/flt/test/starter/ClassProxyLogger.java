package rabbit.flt.test.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.common.spi.ClassProxyListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassProxyLogger implements ClassProxyListener {

    private static Map<String, String> classMap = new ConcurrentHashMap<>();

    @Override
    public synchronized void onProxy(String className) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("found target class: [{}]", className);
        classMap.put(className, "");
    }

    public static Map<String, String> getClassMap() {
        return classMap;
    }
}
