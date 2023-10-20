package rabbit.flt.rpc.test;

import java.util.concurrent.locks.LockSupport;

public interface UserService {

    String getName(String name);

    default void wait5s() {
        LockSupport.parkNanos(5L * 1000 * 1000 * 1000);
    };

    default void exceptionCall(String msg) {
        throw new RuntimeException(msg);
    }
}
