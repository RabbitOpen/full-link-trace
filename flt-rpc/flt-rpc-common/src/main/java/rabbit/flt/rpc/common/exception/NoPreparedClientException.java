package rabbit.flt.rpc.common.exception;

import rabbit.flt.rpc.common.RpcException;

public class NoPreparedClientException extends RpcException {

    public NoPreparedClientException() {
        super("no prepared client exception");
    }
}
