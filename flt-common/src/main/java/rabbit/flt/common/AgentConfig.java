package rabbit.flt.common;

import rabbit.flt.common.exception.AgentException;

import java.lang.reflect.Field;

public class AgentConfig {

    @NotBlank("agent servers 信息不能为空")
    private String servers;

    @NotBlank("应用信息不能为空")
    private String applicationCode;

    @NotBlank("应用密码信息不能为空")
    private String securityKey;

    /**
     * 待扫描磁盘路径，多个路径以逗号分隔
     */
    private String diskSpaceMetricsDirs;

    /**
     * 需要监控流量的网卡，多个网卡逗号分隔开
     */
    private String netMetricsCards = "eth0";

    /**
     * 单次最大传输数据条数
     */
    private int maxTransportBatchSize = 2048;

    /**
     * 最大数据积压条数
     */
    private int maxQueueSize = 1024 * 256;

    /**
     * 只采集metrics信息
     */
    private boolean metricsOnly = false;

    // 是否允许上报gc
    private boolean gcMetricsEnabled = true;

    // 是否允许上报内存
    private boolean memoryMetricsEnabled = true;

    // 是否允许上报环境信息
    private boolean envMetricsEnabled = true;

    // 是否允许上报磁盘
    private boolean diskMetricsEnabled = false;

    // 是否允许上报磁盘io
    private boolean diskIoMetricsEnabled = false;

    // 是否允许上报cpu
    private boolean cpuMetricsEnabled = false;

    // 是否允许上报网络
    private boolean netMetricsEnabled = false;

    // 不需要增强的包，多个包逗号分隔
    private String ignorePackages;

    // 不需要增强的类，多个类逗号分隔
    private String ignoreClasses;

    // 上报线程数
    private int maxReportThreads = 1;

    // 每个节点最大tcp连接数
    private int maxReportConnections = 1;

    // 打印队列长度
    private boolean printQueueLength = false;

    // rpc超时时长
    private int rpcRequestTimeoutSeconds = 30;

    // 超过queue容量的指定百分比时打印队列长度日志
    private double threshold = 0.0;

    /**
     * 校验
     */
    public void doValidation() {
        for (Field field : AgentConfig.class.getDeclaredFields()) {
            NotBlank annotation = field.getAnnotation(NotBlank.class);
            if (null == annotation) {
                continue;
            }
            Object value = null;
            try {
                field.setAccessible(true);
                value = field.get(this);
            } catch (Exception e) {
                // ignore
            }
            if (null == value) {
                throw new AgentException(annotation.value());
            }
        }
    }

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public String getDiskSpaceMetricsDirs() {
        return diskSpaceMetricsDirs;
    }

    public void setDiskSpaceMetricsDirs(String diskSpaceMetricsDirs) {
        this.diskSpaceMetricsDirs = diskSpaceMetricsDirs;
    }

    public String getNetMetricsCards() {
        return netMetricsCards;
    }

    public void setNetMetricsCards(String netMetricsCards) {
        this.netMetricsCards = netMetricsCards;
    }

    public int getMaxTransportBatchSize() {
        return maxTransportBatchSize;
    }

    public void setMaxTransportBatchSize(int maxTransportBatchSize) {
        this.maxTransportBatchSize = maxTransportBatchSize;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public boolean isMetricsOnly() {
        return metricsOnly;
    }

    public void setMetricsOnly(boolean metricsOnly) {
        this.metricsOnly = metricsOnly;
    }

    public boolean isGcMetricsEnabled() {
        return gcMetricsEnabled;
    }

    public void setGcMetricsEnabled(boolean gcMetricsEnabled) {
        this.gcMetricsEnabled = gcMetricsEnabled;
    }

    public boolean isMemoryMetricsEnabled() {
        return memoryMetricsEnabled;
    }

    public void setMemoryMetricsEnabled(boolean memoryMetricsEnabled) {
        this.memoryMetricsEnabled = memoryMetricsEnabled;
    }

    public boolean isEnvMetricsEnabled() {
        return envMetricsEnabled;
    }

    public void setEnvMetricsEnabled(boolean envMetricsEnabled) {
        this.envMetricsEnabled = envMetricsEnabled;
    }

    public boolean isDiskMetricsEnabled() {
        return diskMetricsEnabled;
    }

    public void setDiskMetricsEnabled(boolean diskMetricsEnabled) {
        this.diskMetricsEnabled = diskMetricsEnabled;
    }

    public boolean isDiskIoMetricsEnabled() {
        return diskIoMetricsEnabled;
    }

    public void setDiskIoMetricsEnabled(boolean diskIoMetricsEnabled) {
        this.diskIoMetricsEnabled = diskIoMetricsEnabled;
    }

    public boolean isCpuMetricsEnabled() {
        return cpuMetricsEnabled;
    }

    public void setCpuMetricsEnabled(boolean cpuMetricsEnabled) {
        this.cpuMetricsEnabled = cpuMetricsEnabled;
    }

    public boolean isNetMetricsEnabled() {
        return netMetricsEnabled;
    }

    public void setNetMetricsEnabled(boolean netMetricsEnabled) {
        this.netMetricsEnabled = netMetricsEnabled;
    }

    public String getIgnorePackages() {
        return ignorePackages;
    }

    public void setIgnorePackages(String ignorePackages) {
        this.ignorePackages = ignorePackages;
    }

    public String getIgnoreClasses() {
        return ignoreClasses;
    }

    public void setIgnoreClasses(String ignoreClasses) {
        this.ignoreClasses = ignoreClasses;
    }

    public int getMaxReportThreads() {
        return maxReportThreads < 1 ? 1 : maxReportThreads;
    }

    public void setMaxReportThreads(int maxReportThreads) {
        this.maxReportThreads = maxReportThreads;
    }

    public int getMaxReportConnections() {
        return maxReportConnections;
    }

    public void setMaxReportConnections(int maxReportConnections) {
        this.maxReportConnections = maxReportConnections;
    }

    public boolean showQueueLength() {
        return printQueueLength;
    }

    public void setPrintQueueLength(boolean printQueueLength) {
        this.printQueueLength = printQueueLength;
    }

    public int getRpcRequestTimeoutSeconds() {
        return rpcRequestTimeoutSeconds;
    }

    public void setRpcRequestTimeoutSeconds(int rpcRequestTimeoutSeconds) {
        this.rpcRequestTimeoutSeconds = rpcRequestTimeoutSeconds;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
