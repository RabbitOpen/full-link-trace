package rabbit.flt.rpc.common.rpc;

public interface Authentication {

    /**
     * 认证服务
     * @param applicationCode
     * @param signature
     * @return
     */
    boolean authenticate(String applicationCode, String signature);
}
