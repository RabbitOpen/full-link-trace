package rabbit.flt.rpc.server;

public interface Registrar {

    /**
     * 注册服务
     * @param clz
     * @param handler
     * @param <T>
     */
    <T> void register(Class<T> clz, Object handler);
}
