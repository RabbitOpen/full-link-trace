package rabbit.flt.rpc.client.pool;

import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.common.RpcException;
import rabbit.flt.rpc.common.nio.AbstractClientChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ChannelResourcePool extends AbstractClientChannel implements Client {

    /**
     * 业务线程池
     */
    private ExecutorService workerExecutor;

    /**
     * io线程池，不可执行长期阻塞的任务
     */
    private ExecutorService bossExecutor;

    /**
     * 回调任务线程池
     */
    private ExecutorService callbackExecutor;

    /**
     * 资源守卫
     */
    private ResourceGuard resourceGuard;

    // 操作锁
    private ReentrantLock lock = new ReentrantLock();

    // 选取client时的游标
    private int cursor = 0;

    /**
     * 客户端连接
     */
    private List<ClientChannel> clientChannelList = new ArrayList<>();

    /**
     * 配置
     */
    private PoolConfig poolConfig;

    /**
     * 初始化
     * @param config
     */
    public void init(PoolConfig config) {
        try {
            this.poolConfig = config;
//            if (null == poolConfig)
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }
}
