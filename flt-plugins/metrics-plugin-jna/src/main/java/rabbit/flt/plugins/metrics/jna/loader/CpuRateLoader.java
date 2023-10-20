package rabbit.flt.plugins.metrics.jna.loader;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import rabbit.flt.plugins.metrics.jna.AbstractLoader;

import java.util.concurrent.locks.LockSupport;

public class CpuRateLoader extends AbstractLoader {

    private OSProcess lastCpuInfo;

    private OperatingSystem system;

    private long[] lastSystemTickets;

    // 处理器
    private CentralProcessor processor;

    // cpu 核数
    private int coreCount;

    public CpuRateLoader() {
        SystemInfo systemInfo = new SystemInfo();
        coreCount = systemInfo.getHardware().getProcessor().getLogicalProcessorCount();
        system = systemInfo.getOperatingSystem();
        processor = systemInfo.getHardware().getProcessor();
    }

    /**
     * 获取进程占用的cpu信息
     * @param pid
     * @return
     */
    public double getProcessCpu(int pid) {
        OSProcess process = system.getProcess(pid);
        if (null == lastCpuInfo) {
            lastCpuInfo = process;
            LockSupport.parkNanos(10L * 1000 * 1000);
            return getProcessCpu(pid);
        }
        double ticks = process.getProcessCpuLoadBetweenTicks(lastCpuInfo);
        lastCpuInfo = process;
        return roundHalfUp(ticks / (0 == coreCount ? 1 : coreCount), 4);
    }

    public double getSystemCpu() {
        long[] systemCpuLoadTicks = processor.getSystemCpuLoadTicks();
        if (null == lastSystemTickets) {
            lastSystemTickets = systemCpuLoadTicks;
            LockSupport.parkNanos(10L * 1000 * 1000);
            return getSystemCpu();
        }
        double ticks = processor.getSystemCpuLoadBetweenTicks(lastSystemTickets);
        lastSystemTickets = systemCpuLoadTicks;
        return roundHalfUp(ticks, 4);
    }

    public int getCoreCount() {
        return coreCount;
    }
}
