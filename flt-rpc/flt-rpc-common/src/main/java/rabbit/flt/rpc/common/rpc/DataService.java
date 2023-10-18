package rabbit.flt.rpc.common.rpc;

import rabbit.flt.common.metrics.*;
import rabbit.flt.common.trace.TraceData;

import java.util.List;

public interface DataService {

    /**
     * 处理trace数据
     * @param list
     * @return
     */
    void handleTraceData(List<TraceData> list);

    /**
     * 处理gc数据
     * @param metrics
     */
    void handleGcMetrics(GcMetrics metrics);

    /**
     * 处理内存数据
     * @param metrics
     */
    void handleMemoryMetrics(MemoryMetrics metrics);

    /**
     * 处理环境数据
     * @param metrics
     * @return
     */
    boolean handleEnvironmentMetrics(EnvironmentMetrics metrics);

    /**
     * 处理磁盘空间数据
     * @param metrics
     */
    void handleDiskSpaceMetrics(DiskSpaceMetrics metrics);

    /**
     * 处理磁盘io数据
     * @param metrics
     */
    void handleDiskIoMetrics(DiskIoMetrics metrics);

    /**
     * 处理cpu数据
     * @param metrics
     */
    void handleCpuMetrics(CpuMetrics metrics);

    /**
     * 处理网络io数据
     * @param metrics
     */
    void handleNetworkMetrics(NetworkMetrics metrics);
}
