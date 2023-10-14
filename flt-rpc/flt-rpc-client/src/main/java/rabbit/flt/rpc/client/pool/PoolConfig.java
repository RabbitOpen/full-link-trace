package rabbit.flt.rpc.client.pool;

public class PoolConfig {

    /**
     * boss线程数
     */
    private int bossThreadCount = 1;

    private int workerThreadCount = 1;

    private int acquireClientTimeoutSeconds = 3;

    /**
     * rpc 请求超时时长
     */
    private int rpcRequestTimeoutSeconds = 30;
}
