package rabbit.flt.test.starter;

import rabbit.flt.common.log.LoggerFactory;

public class Slf4jFactory implements LoggerFactory {
    @Override
    public rabbit.flt.common.log.Logger getLogger(String name) {
        return new LoggerProxy(org.slf4j.LoggerFactory.getLogger(name));
    }

    @Override
    public rabbit.flt.common.log.Logger getLogger(Class<?> clz) {
        return new LoggerProxy(org.slf4j.LoggerFactory.getLogger(clz));
    }
}
