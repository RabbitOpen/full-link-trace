package rabbit.flt.rpc.common.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static rabbit.flt.rpc.common.Attributes.WRITE_LOCK;

/**
 * 服务端channel
 */
public abstract class AbstractServerChannel extends ChannelReader {

    /**
     * 客户端连接上来了
     * @param selectionKey
     * @param selector
     */
    public void clientConnected(SelectionKey selectionKey, Selector selector) {
        try {
            ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = channel.accept();
            socketChannel.configureBlocking(false);
            Map<String, Object> attrs = new ConcurrentHashMap<>();
            attrs.put(WRITE_LOCK, new ReentrantLock());
            SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ, attrs);
            if (socketChannel.finishConnect()) {
                onClientConnected(key);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 客户端连接上来了
     * @param selectionKey
     */
    protected abstract void onClientConnected(SelectionKey selectionKey);
}
