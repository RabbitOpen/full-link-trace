package rabbit.flt.rpc.common;

import java.util.concurrent.atomic.AtomicLong;

class RequestCounter {

    private AtomicLong counter = new AtomicLong(0);

    private static final RequestCounter inst = new RequestCounter();

    private RequestCounter() {
    }

    public static long nextId() {
        return inst.counter.addAndGet(1L);
    }
}
