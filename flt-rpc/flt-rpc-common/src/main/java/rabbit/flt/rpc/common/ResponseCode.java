package rabbit.flt.rpc.common;

public final class ResponseCode {

    private ResponseCode() {
    }

    public static final int SUCCESS = 0;

    public static final int FAILED = 1;

    public static final int UN_AUTHENTICATED = 2;

    // 未注册的接口
    public static final int UN_REGISTERED_HANDLER = 3;

    public static final int CHANNEL_CLOSED = 4;
}
