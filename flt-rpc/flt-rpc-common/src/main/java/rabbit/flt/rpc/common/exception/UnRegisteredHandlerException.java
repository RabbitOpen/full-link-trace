package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

public class UnRegisteredHandlerException extends RpcException {

    public UnRegisteredHandlerException(String message) {
        super(message);
    }
}
