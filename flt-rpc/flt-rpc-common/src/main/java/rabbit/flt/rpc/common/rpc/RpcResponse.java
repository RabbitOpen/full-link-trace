package rabbit.flt.rpc.common.rpc;

import rabbit.flt.rpc.common.ResponseCode;

public class RpcResponse<T> {

    // 请求id，响应时返回回去
    private long requestId;

    // 数据
    private T data;

    private int code = ResponseCode.SUCCESS;

    private boolean success = true;

    // 异常消息
    private String msg;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
