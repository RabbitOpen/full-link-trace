package rabbit.flt.common;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * 指标
 */
public abstract class Metrics {

    // 进程名
    private String processName;

    // 主机ip
    private String host;

    // 应用编码
    private String applicationCode;

    /**
     * 上线时间
     */
    private Long onlineTime;

    public Metrics() {
        setHost(getHostIp());
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        setProcessName(runtimeMXBean.getName());
        setOnlineTime(runtimeMXBean.getStartTime());
    }

    /**
     * 指标类型
     * @return
     */
    public abstract String getMetricsType();

    /**
     * 获取主机ip
     *
     * @return
     */
    public static String getHostIp() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "UNKNOWN HOST";
        }
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public Long getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(Long onlineTime) {
        this.onlineTime = onlineTime;
    }
}
