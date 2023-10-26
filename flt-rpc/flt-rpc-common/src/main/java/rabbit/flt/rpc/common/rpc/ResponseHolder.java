package rabbit.flt.rpc.common.rpc;

import reactor.core.publisher.MonoSink;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ResponseHolder<T> implements Consumer<MonoSink<T>> {

    private MonoSink<T> sink;

    private T response;

    private ReentrantLock lock = new ReentrantLock();

    @Override
    public void accept(MonoSink<T> tMonoSink) {
        try {
            lock.lock();
            if (null != response) {
                tMonoSink.success(response);
            } else {
                this.sink = tMonoSink;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setResponse(T resp) {
        try {
            lock.lock();
            if (null == sink) {
                response = resp;
            } else {
                this.sink.success(resp);
            }
        } finally {
            lock.unlock();
        }
    }
}
