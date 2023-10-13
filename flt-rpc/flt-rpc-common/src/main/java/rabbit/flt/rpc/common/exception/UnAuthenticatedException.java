package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

public class UnAuthenticatedException extends RpcException {

    public UnAuthenticatedException() {
        this("UnAuthenticated exception");
    }

    public UnAuthenticatedException(String message) {
        super(message);
    }
}
