package rabbit.flt.rpc.server;

import java.net.SocketOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class ServerBuilder {

    private List<Consumer<Server>> operations = new ArrayList<>();

    /**
     * 设置端口
     * @param port
     * @return
     */
    public ServerBuilder port(int port) {
        operations.add(s -> s.setPort(port));
        return this;
    }

    /**
     * 设置业务线程数
     * @param workerThreadCount
     * @return
     */
    public ServerBuilder workerThreadCount(int workerThreadCount) {
        operations.add(s -> s.setWorkerThreadCount(workerThreadCount));
        return this;
    }

    /**
     * 设置boss线程数
     * @param bossThreadCount
     * @return
     */
    public ServerBuilder bossThreadCount(int bossThreadCount) {
        operations.add(s -> s.setBossThreadCount(bossThreadCount));
        return this;
    }

    /**
     * 注册rpc接口
     * @param clz
     * @param handler
     * @param <T>
     * @return
     */
    public <T> ServerBuilder registerHandler(Class<T> clz, T handler) {
        operations.add(s -> s.register(clz, handler));
        return this;
    }

    /**
     * 设置最大挂起连接数
     * @param maxPendingConnections
     * @return
     */
    public ServerBuilder maxPendingConnections(int maxPendingConnections) {
        operations.add(s -> s.setMaxPendingConnections(maxPendingConnections));
        return this;
    }

    /**
     * 设置最大帧长度
     * @param maxFrameLength
     * @return
     */
    public ServerBuilder maxFrameLength(int maxFrameLength) {
        operations.add(s -> s.setMaxFrameLength(maxFrameLength));
        return this;
    }

    /**
     * 设置boss线程池
     * @param bossExecutor
     * @return
     */
    public ServerBuilder bossExecutor(ExecutorService bossExecutor) {
        operations.add(s -> s.setBossExecutor(bossExecutor));
        return this;
    }

    /**
     * 设置业务线程池
     * @param workerExecutor
     * @return
     */
    public ServerBuilder workerExecutor(ExecutorService workerExecutor) {
        operations.add(s -> s.setWorkerExecutor(workerExecutor));
        return this;
    }

    /**
     * 设置host
     * @param host
     * @return
     */
    public ServerBuilder host(String host) {
        operations.add(s -> s.setHost(host));
        return this;
    }

    /**
     * 设置tcp参数
     * @param key
     * @param value
     * @return
     */
    public ServerBuilder socketOption(SocketOption<Integer> key, Integer value) {
        operations.add(s -> s.setSocketOption(key, value));
        return this;
    }

    public static ServerBuilder builder() {
        return new ServerBuilder();
    }

    /**
     * do build
     * @return
     */
    public Server build() {
        Server server = new Server();
        operations.forEach(op -> op.accept(server));
        return server;
    }
}
