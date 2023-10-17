package rabbit.flt.common.metrics.info;

public class DiskIoInfo {

    /**
     * 设备名
     */
    private String deviceName;

    /**
     * 每秒读取次数
     */
    private Long readTimesPerSec;

    /**
     * 每秒写入次数
     */
    private Long writeTimesPerSec;

    /**
     * 每秒读取字节
     */
    private Long readBytesPerSec;

    /**
     * 每秒写入字节
     */
    private Long writeBytesPerSec;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Long getReadTimesPerSec() {
        return readTimesPerSec;
    }

    public void setReadTimesPerSec(Long readTimesPerSec) {
        this.readTimesPerSec = readTimesPerSec;
    }

    public Long getWriteTimesPerSec() {
        return writeTimesPerSec;
    }

    public void setWriteTimesPerSec(Long writeTimesPerSec) {
        this.writeTimesPerSec = writeTimesPerSec;
    }

    public Long getReadBytesPerSec() {
        return readBytesPerSec;
    }

    public void setReadBytesPerSec(Long readBytesPerSec) {
        this.readBytesPerSec = readBytesPerSec;
    }

    public Long getWriteBytesPerSec() {
        return writeBytesPerSec;
    }

    public void setWriteBytesPerSec(Long writeBytesPerSec) {
        this.writeBytesPerSec = writeBytesPerSec;
    }
}
