package rabbit.flt.rpc.common.nio;

import java.nio.channels.SelectionKey;

public interface ChannelAdaptor {

    /**
     * 处理读取到的数据
     * @param selectionKey
     * @param data           数据
     * @param transferSize   传输大小（如果是压缩过的，该size通常小于真实大小）
     */
    void processChannelData(SelectionKey selectionKey, byte[] data, int transferSize);

    /**
     * 连接断开事件
     * @param selectionKey
     */
    void disconnected(SelectionKey selectionKey);

    /**
     * 关闭
     */
    void close();

    /**
     * 重置选择器（关闭旧的，新建一个）
     * @return
     */
    SelectorWrapper resetSelector();
}
