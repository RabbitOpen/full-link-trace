package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

public class ChannelClosedException extends RpcException {

    public ChannelClosedException() {
    }

    public ChannelClosedException(String message) {
        super(message);
    }
}
