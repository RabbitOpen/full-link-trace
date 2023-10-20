package rabbit.flt.rpc.client;

import rabbit.flt.rpc.client.pool.PoolConfig;
import rabbit.flt.rpc.client.pool.SecureChannelResourcePool;

public class AgentRequestFactory extends RequestFactory {

    private SecureChannelResourcePool resourcePool = new SecureChannelResourcePool();

    private boolean initialized = false;

    @Override
    protected Client getClient() {
        return resourcePool;
    }

    /**
     * 初始化
     * @param config
     */
    public synchronized void init(PoolConfig config) {
        if (initialized) {
            return;
        }
        initialized = true;
        this.timeoutSeconds = config.getRpcRequestTimeoutSeconds();
        this.maxRetryTime = config.getMaxRetryTime();
        this.resourcePool.init(config);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.resourcePool.close()));
    }

}
