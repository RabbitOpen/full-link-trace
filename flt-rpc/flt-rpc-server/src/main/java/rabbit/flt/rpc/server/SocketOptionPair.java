package rabbit.flt.rpc.server;

import java.net.SocketOption;

public class SocketOptionPair<T> {

    private SocketOption<T> key;

    private T value;

    public SocketOptionPair(SocketOption<T> key, T value) {
        this.key = key;
        this.value = value;
    }

    public SocketOption<T> getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
