package rabbit.flt.common.metrics;

import rabbit.flt.common.Metrics;
import rabbit.flt.common.MetricsType;

import java.util.HashMap;
import java.util.Map;

public class GcMetrics extends Metrics {

    /**
     * gc开始时间
     */
    private long start;

    /**
     * gc耗时
     */
    private long cost;

    /**
     * gc总次数
     */
    private long total;

    /**
     * gc总耗时
     */
    private long totalCost;

    /**
     * gc原因
     */
    private String cause;

    /**
     * 名字
     */
    private String gcName;

    /**
     * gc action
     */
    private String gcAction;

    /**
     * 变化明细，key时内存区间，value是内存变化
     */
    private Map<String, String> detail = new HashMap<>();

    @Override
    public String getMetricsType() {
        return MetricsType.GC.name();
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getGcName() {
        return gcName;
    }

    public void setGcName(String gcName) {
        this.gcName = gcName;
    }

    public String getGcAction() {
        return gcAction;
    }

    public void setGcAction(String gcAction) {
        this.gcAction = gcAction;
    }

    public Map<String, String> getDetail() {
        return detail;
    }

    public void setDetail(Map<String, String> detail) {
        this.detail = detail;
    }
}
