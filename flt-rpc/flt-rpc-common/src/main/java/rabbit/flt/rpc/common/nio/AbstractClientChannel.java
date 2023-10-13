package rabbit.flt.rpc.common.nio;

import rabbit.flt.rpc.common.Attributes;
import rabbit.flt.rpc.common.ChannelStatus;
import rabbit.flt.rpc.common.ServerNode;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static rabbit.flt.rpc.common.ChannelStatus.INIT;

/**
 * 客户端channel
 */
public abstract class AbstractClientChannel extends ChannelReader {

    private ChannelStatus channelStatus = INIT;

    /**
     * 跟服务端连接上了
     * @param selectionKey
     */
    public void serverConnected(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        try {
            if (channel.finishConnect()) {
                selectionKey.interestOps(SelectionKey.OP_READ);
                this.onServerConnected(selectionKey);
            }
        } catch (Exception t) {
            connectFailed(selectionKey, t);
            selectionKey.channel();
            close(channel);
        }
    }

    /**
     * 连接失败
     * @param selectionKey
     * @param t
     */
    public abstract void connectFailed(SelectionKey selectionKey, Throwable t);

    /**
     * 连接成功
     * @param selectionKey
     */
    public abstract void onServerConnected(SelectionKey selectionKey);

    /**
     * 获取key上绑定的client信息
     *
     * @param selectionKey
     * @return
     */
    public final AbstractClientChannel getClientChannel(SelectionKey selectionKey) {
        Map<String, Object> attrs = (Map<String, Object>) selectionKey.attachment();
        return (AbstractClientChannel) attrs.get(Attributes.NIO_CLIENT);
    }

    /**
     * 获取连接上的服务器节点
     *
     * @return
     */
    protected abstract ServerNode getServerNode();

    public ChannelStatus getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(ChannelStatus channelStatus) {
        this.channelStatus = channelStatus;
    }
}
