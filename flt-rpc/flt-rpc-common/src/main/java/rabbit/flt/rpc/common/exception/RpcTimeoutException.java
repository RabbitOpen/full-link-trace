package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

public class RpcTimeoutException extends RpcException {

    public RpcTimeoutException(long requestId, int seconds) {
        super(String.format("rpc request[%d] timeout [%ds] exception", requestId, seconds));
    }
}
