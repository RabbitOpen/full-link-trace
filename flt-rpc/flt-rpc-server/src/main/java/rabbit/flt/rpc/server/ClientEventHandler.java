package rabbit.flt.rpc.server;

import java.nio.channels.SelectionKey;

public interface ClientEventHandler {

    /**
     * 客户端连接上了
     * @param key
     */
    default void onClientConnected(SelectionKey key) {}

    /**
     * 客户端关闭了
     * @param key
     */
    default void onClientClosed(SelectionKey key) {}
}
