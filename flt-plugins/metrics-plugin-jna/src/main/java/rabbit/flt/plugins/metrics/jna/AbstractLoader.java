package rabbit.flt.plugins.metrics.jna;

import java.math.BigDecimal;

public abstract class AbstractLoader {

    /**
     * 四舍五入
     * @param data
     * @param scale 精度
     * @return
     */
    protected double roundHalfUp(double data, int scale) {
        return BigDecimal.valueOf(data).setScale(scale, BigDecimal.ROUND_CEILING).doubleValue();
    }
}
