package rabbit.flt.rpc.common;

import java.beans.Transient;
import java.util.concurrent.Semaphore;

public class Protocol<T> {

    /**
     * 请求id
     */
    private long requestId;

    // 请求
    private T request;

    private transient Semaphore semaphore;

    public Protocol() {
    }

    public Protocol(T request) {
        this();
        this.request = request;
        this.semaphore = new Semaphore(0);
    }

    @Transient
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
