package rabbit.flt.rpc.common.exception;

import rabbit.flt.common.utils.StringUtils;

/**
 * 认证失败
 */
public class AuthenticationFailedException extends AuthenticationException {

    public AuthenticationFailedException(Throwable cause) {
        super(StringUtils.isEmpty(cause.getMessage()) ? "authentication failed! " : "authentication failed! ".concat(cause.getMessage()));
    }

}
