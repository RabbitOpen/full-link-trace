package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

public class ChannelReadException extends RpcException {

    public ChannelReadException(String message) {
        super(message);
    }
}
