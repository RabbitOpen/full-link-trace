package rabbit.flt.rpc.client.pool;

public interface ChannelListener {

    /**
     * channel建立后置事件
     * @param channel
     */
    void afterCreated(ClientChannel channel);
}
