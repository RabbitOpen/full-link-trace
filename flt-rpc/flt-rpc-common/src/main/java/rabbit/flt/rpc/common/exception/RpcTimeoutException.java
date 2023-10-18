package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

public class RpcTimeoutException extends RpcException {

    public RpcTimeoutException(String request, long requestId, int seconds) {
        super(String.format("rpc request[%s][%d] timeout[%ds] exception", request, requestId, seconds));
    }
}
