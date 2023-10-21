package rabbit.flt.rpc.server.proxy;

import rabbit.flt.rpc.common.exception.AuthenticationException;
import rabbit.flt.rpc.server.RequestDispatcher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class RpcRequestHandler implements InvocationHandler {

    protected Object realHandler;

    public RpcRequestHandler(Object realHandler) {
        this.realHandler = realHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!isAuthenticated()) {
            throw new AuthenticationException();
        }
        try {
            return method.invoke(realHandler, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private boolean isAuthenticated() {
        SelectionKey selectionKey = RequestDispatcher.getCurrentSelectionKey();
        Map<String, Object> attrs = (Map<String, Object>) selectionKey.attachment();
        return attrs.containsKey(AuthenticationHandler.AUTHENTICATE);
    }
}
