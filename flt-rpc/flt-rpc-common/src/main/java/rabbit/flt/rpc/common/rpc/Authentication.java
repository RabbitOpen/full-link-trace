package rabbit.flt.rpc.common.rpc;

import rabbit.flt.rpc.common.exception.AuthenticationException;

public interface Authentication {

    /**
     * 认证服务(失败会抛异常)
     * @param applicationCode
     * @param signature
     * @return
     */
    void authenticate(String applicationCode, String signature) throws AuthenticationException;
}
