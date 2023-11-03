package rabbit.flt.rpc.common.nio;

import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * io 事件处理器
 */
public class ChannelProcessor extends Thread {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    private SelectorWrapper selectorWrapper;

    private ChannelReader channelReader;

    private volatile boolean closed = false;

    public ChannelProcessor(SelectorWrapper selectorWrapper, ChannelReader channelReader) {
        this.selectorWrapper = selectorWrapper;
        this.channelReader = channelReader;
        setDaemon(false);
        setName("channel-processor");
    }

    @Override
    public void run() {
        int emptyEpollCount = 0;
        while (true) {
            try {
                if (closed) {
                    break;
                }
                Selector selector = selectorWrapper.getSelector();
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                selectorWrapper.executeHooks();
                if (keys.isEmpty()) {
                    emptyEpollCount++;
                    if (100 == emptyEpollCount) {
                        rebuildSelectorWhenEpollBugFound();
                    }
                } else {
                    emptyEpollCount = 0;
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        handleSelectionEvent(selectionKey);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void rebuildSelectorWhenEpollBugFound() {
        logger.warn("epoll bug found, rebuild selector!");
        selectorWrapper = channelReader.resetSelector();
    }

    public synchronized void close() {
        if (closed) {
            return;
        }
        try {
            closed = true;
            selectorWrapper.wakeup();
            join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 处理io事件
     * @param selectionKey
     */
    private void handleSelectionEvent(SelectionKey selectionKey) {
        try {
            if (!selectionKey.isValid()) {
                return;
            }
            if (selectionKey.isAcceptable()) {
                ((AbstractServerChannel)channelReader).clientConnected(selectionKey,
                        selectorWrapper.getSelector());
            } else if (selectionKey.isConnectable()) {
                ((AbstractClientChannel)channelReader).serverConnected(selectionKey);
            } else if (selectionKey.isReadable()) {
                channelReader.readChannelData(selectionKey);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
