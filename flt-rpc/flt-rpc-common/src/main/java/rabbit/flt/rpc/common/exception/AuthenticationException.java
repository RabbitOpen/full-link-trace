package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

public class AuthenticationException extends RpcException {

    public AuthenticationException() {
        this("UnAuthenticated request");
    }

    public AuthenticationException(String message) {
        super(message);
    }
}
