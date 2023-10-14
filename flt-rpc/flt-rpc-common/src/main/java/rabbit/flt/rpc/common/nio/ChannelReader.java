package rabbit.flt.rpc.common.nio;

import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.rpc.common.DataType;
import rabbit.flt.rpc.common.GzipUtil;
import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.SelectorResetListener;
import rabbit.flt.rpc.common.Serializer;
import rabbit.flt.rpc.common.exception.BeyondLimitException;
import rabbit.flt.rpc.common.exception.ChannelClosedException;
import rabbit.flt.rpc.common.exception.ChannelReadException;

import java.io.Closeable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

public abstract class ChannelReader implements ChannelAdaptor {

    protected Logger logger = AgentLoggerFactory.getLogger(getClass());

    protected SelectorWrapper selectorWrapper;

    // 最大帧长度
    protected int maxFrameLength = 16 * 1024 * 1024;

    /**
     * 重置selector
     *
     * @return
     */
    @Override
    public final SelectorWrapper resetSelector() {
        try {
            // !!!!此处有bug
            Selector newSelector = Selector.open();
            if (null == selectorWrapper) {
                selectorWrapper = new SelectorWrapper();
                return selectorWrapper;
            }
            Selector oldSelector = this.selectorWrapper.getSelector();
            for (SelectionKey oldKey : oldSelector.keys()) {
                if (!oldKey.isAcceptable()) {
                    continue;
                }
                SelectionKey newKey = oldKey.channel().register(newSelector, oldKey.interestOps(), oldKey.attachment());
                getSelectorResetListener().keyChanged(oldKey, newKey);
            }
        } catch (Exception e) {
            throw new RpcException(e);
        }
        return selectorWrapper;
    }

    /**
     * 读取channel上的数据
     *
     * @param selectionKey
     */
    public void readChannelData(SelectionKey selectionKey) {
        selectionKey.interestOps(0);
        getBossExecutor().submit(() -> {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            try {
                ByteBuffer buffer = ByteBuffer.allocate(12);
                int frameLength = readFrameLength(channel, buffer);
                byte[] dataBytes = readByteData(channel, buffer, frameLength);
                handleData(selectionKey, dataBytes, frameLength);
                wakeupSelectionKey(selectionKey);
            } catch (ChannelClosedException e) {
                if (this instanceof AbstractClientChannel) {
                    logger.info("server[{}] is closed!", ((AbstractClientChannel) this).getClientChannel(selectionKey).getServerNode());
                } else {
                    logger.info("client[{}] is closed!", getRemoteAddress(channel));
                }
                disconnected(selectionKey);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                disconnected(selectionKey);
            }
        });
    }

    /**
     * 读取数据字节
     *
     * @param channel
     * @param buffer
     * @param frameLength
     * @return
     */
    private byte[] readByteData(SocketChannel channel, ByteBuffer buffer, int frameLength) {
        if (frameLength > getMaxFrameLength()) {
            throw new BeyondLimitException(getMaxFrameLength(), getRemoteAddress(channel));
        }
        boolean gzipped = DataType.GZIPPED == buffer.getInt();
        int originalSize = 0;
        if (gzipped) {
            originalSize = buffer.getInt();
            int maxRealSize = getMaxFrameLength() * 2;
            if (originalSize > maxRealSize) {
                throw new BeyondLimitException(maxRealSize, getRemoteAddress(channel));
            }
        }
        buffer = ByteBuffer.allocateDirect(frameLength);
        readInputData(channel, buffer);
        byte[] array = new byte[frameLength];
        buffer.position(0);
        buffer.get(array);
        if (gzipped) {
            array = GzipUtil.decompress(array, originalSize);
        }
        return array;
    }

    /**
     * 关闭资源
     *
     * @param resource
     */
    protected void close(Closeable resource) {
        Serializer.close(resource);
    }

    /**
     * 处理boss线程读取到的数据
     *
     * @param selectionKey
     * @param data
     * @param transferSize
     */
    protected void handleData(SelectionKey selectionKey, byte[] data, int transferSize) {
        getWorkerExecutor().submit(() -> processChannelData(selectionKey, data, transferSize));
    }

    /**
     * 唤醒key
     *
     * @param selectionKey
     */
    private void wakeupSelectionKey(SelectionKey selectionKey) {
        try {
            if (selectionKey.isValid()) {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
            selectorWrapper.wakeup();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    /**
     * 读取帧长度
     *
     * @param channel
     * @param buffer
     * @return
     */
    private int readFrameLength(SocketChannel channel, ByteBuffer buffer) {
        readInputData(channel, buffer);
        buffer.position(0);
        return buffer.getInt();
    }

    /**
     * 读取输入数据, 读完数据或者读满buffer就返回
     *
     * @param channel
     * @param buffer
     */
    private void readInputData(SocketChannel channel, ByteBuffer buffer) {
        long start = System.currentTimeMillis();
        while (true) {
            int read;
            try {
                read = channel.read(buffer);
            } catch (Exception e) {
                throw new ChannelClosedException(e.getMessage());
            }
            if (-1 == read) {
                // 正常关闭
                throw new ChannelClosedException();
            }
            if (0 == buffer.remaining()) {
                break;
            }
            if (0 == read) {
                LockSupport.parkNanos(1000L * 1000);
                if (System.currentTimeMillis() - start > 10 * 1000) {
                    throw new ChannelReadException("read time out exception, no data is read beyond 10s");
                }
            } else {
                start = System.currentTimeMillis();
            }
        }
    }

    private SocketAddress getRemoteAddress(SocketChannel channel) {
        try {
            return channel.getRemoteAddress();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取重置事件监听器
     *
     * @return
     */
    public abstract SelectorResetListener getSelectorResetListener();

    /**
     * 获取工作线程池
     *
     * @return
     */
    protected abstract ExecutorService getWorkerExecutor();

    /**
     * 获取boss线程池
     *
     * @return
     */
    protected abstract ExecutorService getBossExecutor();

    /**
     * 获取最大帧长度
     *
     * @return
     */
    protected final int getMaxFrameLength() {
        return maxFrameLength;
    }
}
