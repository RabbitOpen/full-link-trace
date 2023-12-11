package rabbit.flt.rpc.common.nio;

/**
 * 协议帧
 */
public class FrameProtocol {

    /**
     * 内容长度
     */
    private int contentLength;

    /**
     * 是否压缩
     */
    private boolean compressed;

    /**
     * 明文长度
     */
    private int plainTextLength;

    public FrameProtocol(int contentLength, boolean compressed, int plainTextLength) {
        this.contentLength = contentLength;
        this.compressed = compressed;
        this.plainTextLength = plainTextLength;
    }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public int getPlainTextLength() {
        return plainTextLength;
    }
}
