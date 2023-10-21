package rabbit.flt.rpc.common.exception;

/**
 * 认证失败
 */
public class AuthenticationFailedException extends AuthenticationException {

    public AuthenticationFailedException() {
        super("authentication failed!");
    }

}
