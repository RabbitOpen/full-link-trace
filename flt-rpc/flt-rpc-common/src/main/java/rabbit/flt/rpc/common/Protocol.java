package rabbit.flt.rpc.common;

import com.esotericsoftware.kryo.serializers.FieldSerializer;

import java.util.concurrent.Semaphore;

public class Protocol<T> {

    /**
     * 请求id
     */
    private long requestId;

    // 请求
    private T request;

    @FieldSerializer.Optional("Protocol.semaphore")
    private Semaphore semaphore;

    public Protocol() {
    }

    public Protocol(T request) {
        this();
        this.request = request;
        this.semaphore = new Semaphore(0);
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void increaseRequestId() {
        setRequestId(RequestCounter.nextId());
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }
}
