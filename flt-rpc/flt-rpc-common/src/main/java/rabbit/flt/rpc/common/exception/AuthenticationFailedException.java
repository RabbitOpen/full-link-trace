package rabbit.flt.rpc.common.exception;

/**
 * 认证失败
 */
public class AuthenticationFailedException extends AuthenticationException {

    public AuthenticationFailedException(Throwable cause) {
        super("authentication failed! ".concat(cause.getMessage()));
    }

}
