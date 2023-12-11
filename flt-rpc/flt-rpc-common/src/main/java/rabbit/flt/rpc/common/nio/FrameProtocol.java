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
    private boolean gzipped;

    /**
     * 明文长度
     */
    private int plainContentLength;

    public FrameProtocol(int contentLength, boolean gzipped, int plainContentLength) {
        this.contentLength = contentLength;
        this.gzipped = gzipped;
        this.plainContentLength = plainContentLength;
    }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isGzipped() {
        return gzipped;
    }

    public int getPlainContentLength() {
        return plainContentLength;
    }
}
