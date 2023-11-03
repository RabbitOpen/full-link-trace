package rabbit.flt.rpc.common.nio;

import rabbit.flt.common.utils.ResourceUtils;
import rabbit.flt.rpc.common.RpcException;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ArrayBlockingQueue;

public class SelectorWrapper {

    private Selector selector;

    /**
     * 回调任务队列
     */
    private ArrayBlockingQueue<Runnable> hooks = new ArrayBlockingQueue<>(1024);

    public SelectorWrapper() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RpcException(e);
        }
    }

    /**
     * 添加hook，然后换新selector
     * @param hook
     */
    public void addHookJob(Runnable hook) {
        this.hooks.add(hook);
        wakeup();
    }

    /**
     * 执行回调
     */
    public void executeHooks() {
        while (true) {
            Runnable hook = hooks.poll();
            if (null == hook) {
                return;
            }
            hook.run();
        }
    }

    public void wakeup() {
        getSelector().wakeup();
    }

    public Selector getSelector() {
        return selector;
    }

    public void close() {
        ResourceUtils.close(selector);
    }
}
