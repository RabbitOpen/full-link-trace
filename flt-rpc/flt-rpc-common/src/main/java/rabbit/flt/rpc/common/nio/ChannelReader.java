package rabbit.flt.rpc.common.nio;

import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.utils.GZipUtils;
import rabbit.flt.common.utils.ResourceUtils;
import rabbit.flt.rpc.common.DataType;
import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.SelectorResetListener;
import rabbit.flt.rpc.common.ServerNode;
import rabbit.flt.rpc.common.exception.BeyondLimitException;
import rabbit.flt.rpc.common.exception.ChannelClosedException;
import rabbit.flt.rpc.common.exception.ChannelReadException;
import rabbit.flt.rpc.common.exception.EndPointClosedException;

import java.io.Closeable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;

public abstract class ChannelReader implements ChannelAdaptor {

    protected Logger logger = AgentLoggerFactory.getLogger(getClass());

    protected SelectorWrapper selectorWrapper;

    // 最大帧长度
    protected int maxFrameLength = 16 * 1024 * 1024;

    private LinkedBlockingQueue<ByteBuffer> cachedByteBuffer;

    public ChannelReader() {
        cachedByteBuffer = new LinkedBlockingQueue();
    }

    /**
     * 重置selector
     *
     * @return
     */
    @Override
    public final SelectorWrapper resetSelector() {
        try {
            if (null == selectorWrapper) {
                selectorWrapper = new SelectorWrapper();
                return selectorWrapper;
            }
            SelectorWrapper oldSelector = this.selectorWrapper;
            this.selectorWrapper = new SelectorWrapper();
            for (SelectionKey oldKey : oldSelector.getSelector().keys()) {
                if (!oldKey.isValid()) {
                    continue;
                }
                SelectionKey newKey = oldKey.channel().register(selectorWrapper.getSelector(),
                        oldKey.interestOps(), oldKey.attachment());
                getSelectorResetListener().keyChanged(oldKey, newKey);
            }
            oldSelector.close();
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
            } catch (EndPointClosedException e) {
                channelClosed(selectionKey, () -> {
                    ServerNode serverNode = ((AbstractClientChannel) this).getClientChannel(selectionKey).getServerNode();
                    serverNodeClosed(serverNode);
                });
                disconnected(selectionKey);
            } catch (ChannelClosedException e) {
                channelClosed(selectionKey, () -> {
                    // do nothing
                });
                disconnected(selectionKey);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                disconnected(selectionKey);
            }

        });
    }

    private void channelClosed(SelectionKey selectionKey, Runnable clientCallBack) {
        if (this instanceof AbstractClientChannel) {
            ServerNode serverNode = ((AbstractClientChannel) this).getClientChannel(selectionKey).getServerNode();
            logger.info("server[{}] is closed!", serverNode);
            clientCallBack.run();
        } else {
            logger.info("client[{}] is closed!", getRemoteAddress((SocketChannel) selectionKey.channel()));
        }
    }

    /**
     * 服务器节点关闭
     *
     * @param serverNode
     */
    protected void serverNodeClosed(ServerNode serverNode) {

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
        ByteBuffer buf = getCachedByteBuffer(frameLength);
        try {
            readInputData(channel, buf);
            byte[] array = new byte[frameLength];
            buf.position(0);
            buf.get(array);
            if (gzipped) {
                array = GZipUtils.decompress(array, originalSize);
            }
            return array;
        } finally {
            cachedByteBuffer.add(buf);
        }
    }

    /**
     * 使用缓存的直接内存
     * @param frameLength
     * @return
     */
    private ByteBuffer getCachedByteBuffer(int frameLength) {
        ByteBuffer buffer = cachedByteBuffer.poll();
        if (null == buffer) {
            /**
             * 此处最大分配次数受 boss executor 线程数个数限制，不会超过boss executor 线程数
             */
            buffer = ByteBuffer.allocate(getMaxFrameLength());
        }
        buffer.clear();
        buffer.limit(frameLength);
        return buffer;
    }

    /**
     * 关闭资源
     *
     * @param resource
     */
    protected void close(Closeable resource) {
        ResourceUtils.close(resource);
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
        } catch (Exception t) {
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
                // 对端正常关闭
                throw new EndPointClosedException();
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

    public SelectorWrapper getWrapper() {
        return selectorWrapper;
    }
}
