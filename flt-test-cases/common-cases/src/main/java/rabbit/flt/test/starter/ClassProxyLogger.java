package rabbit.flt.test.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.plugins.common.spi.ClassProxyListener;

public class ClassProxyLogger implements ClassProxyListener {

    private Logger logger = LoggerFactory.getLogger("transformer");

    @Override
    public void onProxy(String className) {
        logger.info("found target class: [{}]", className);
    }
}
