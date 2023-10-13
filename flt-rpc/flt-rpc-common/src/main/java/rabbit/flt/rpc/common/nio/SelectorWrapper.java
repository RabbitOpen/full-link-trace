package rabbit.flt.rpc.common.nio;

import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.rpc.common.Hook;
import rabbit.flt.rpc.common.Serializer;

import java.nio.channels.Selector;
import java.util.concurrent.ArrayBlockingQueue;

public class SelectorWrapper {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    private Selector selector;

    /**
     * 回调任务队列
     */
    private ArrayBlockingQueue<Hook> hooks = new ArrayBlockingQueue<>(1024);

    public SelectorWrapper(Selector selector) {
        this.selector = selector;
    }

    /**
     * 添加hook，然后换新selector
     * @param hook
     */
    public void addHookJob(Hook hook) {
        this.hooks.add(hook);
        wakeup();
    }

    /**
     * 执行回调
     */
    public void executeHooks() {
        while (true) {
            Hook hook = hooks.poll();
            if (null == hook) {
                return;
            }
            try {
                hook.run();
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    public void wakeup() {
        getSelector().wakeup();
    }

    public Selector getSelector() {
        return selector;
    }

    public void close() {
        Serializer.close(selector);
    }
}
