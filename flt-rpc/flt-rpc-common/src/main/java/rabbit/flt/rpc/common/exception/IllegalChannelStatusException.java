package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.ChannelStatus;
import rabbit.flt.rpc.common.RpcException;

public class IllegalChannelStatusException extends RpcException {

    public IllegalChannelStatusException(ChannelStatus status) {
        super("illegal channel status: " + status.getName());
    }
}
