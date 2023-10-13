package rabbit.flt.rpc.common;

public interface ResponseCode {

    int SUCCESS = 0;

    int FAILED = 1;

    int UN_AUTHENTICATED = 2;

    // 未注册的接口
    int UN_REGISTERED_HANDLER = 3;

    int CHANNEL_CLOSED = 4;
}
