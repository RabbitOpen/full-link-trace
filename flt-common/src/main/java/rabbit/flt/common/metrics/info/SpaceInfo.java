package rabbit.flt.common.metrics.info;

public class SpaceInfo {

    /**
     * 总大小   单位： 字节
     */
    private long total;

    private long used;

    /**
     * 磁盘目录
     */
    private String dir;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
