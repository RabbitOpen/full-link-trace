package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

/**
 * 认证失败
 */
public class AuthenticationFailedException extends RpcException {

    public AuthenticationFailedException() {
        super("authentication failed!");
    }
}
