package rabbit.flt.common.metrics.info;

public class NetFlowInfo {

    /**
     * 网卡名
     */
    private String netCard;

    /**
     * 每秒收到的包个数
     */
    private Double rxPktPerSec;

    /**
     * 每秒发送的包个数
     */
    private Double txPktPerSec;

    /**
     * 每秒收到的字节
     */
    private Double rxBytesPerSec;

    /**
     * 每秒发送的字节
     */
    private Double txBytesPerSec;

    public String getNetCard() {
        return netCard;
    }

    public void setNetCard(String netCard) {
        this.netCard = netCard;
    }

    public Double getRxPktPerSec() {
        return rxPktPerSec;
    }

    public void setRxPktPerSec(Double rxPktPerSec) {
        this.rxPktPerSec = rxPktPerSec;
    }

    public Double getTxPktPerSec() {
        return txPktPerSec;
    }

    public void setTxPktPerSec(Double txPktPerSec) {
        this.txPktPerSec = txPktPerSec;
    }

    public Double getRxBytesPerSec() {
        return rxBytesPerSec;
    }

    public void setRxBytesPerSec(Double rxBytesPerSec) {
        this.rxBytesPerSec = rxBytesPerSec;
    }

    public Double getTxBytesPerSec() {
        return txBytesPerSec;
    }

    public void setTxBytesPerSec(Double txBytesPerSec) {
        this.txBytesPerSec = txBytesPerSec;
    }
}
