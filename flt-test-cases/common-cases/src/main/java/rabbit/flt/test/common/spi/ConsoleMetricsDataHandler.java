package rabbit.flt.test.common.spi;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsDataHandler;

public class ConsoleMetricsDataHandler implements MetricsDataHandler {

//    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean handle(Metrics data) {
//        logger.info("received metrics: {}", data.getMetricsType());
        return true;
    }

    @Override
    public int getPriority() {
        return 1000;
    }

}
