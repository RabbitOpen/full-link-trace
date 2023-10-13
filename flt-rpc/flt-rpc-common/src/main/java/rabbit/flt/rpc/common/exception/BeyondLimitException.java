package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

import java.net.SocketAddress;

public class BeyondLimitException extends RpcException {

    public BeyondLimitException(int limit, SocketAddress address) {
        super("data from [" + address + "] is beyond max frame size[" + limit + "]");
    }
}
