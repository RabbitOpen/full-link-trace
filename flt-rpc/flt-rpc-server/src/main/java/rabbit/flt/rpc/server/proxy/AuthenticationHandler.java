package rabbit.flt.rpc.server.proxy;

import rabbit.flt.common.utils.StringUtil;
import rabbit.flt.rpc.common.Request;
import rabbit.flt.rpc.common.exception.AuthenticationFailedException;
import rabbit.flt.rpc.common.rpc.Authentication;
import rabbit.flt.rpc.server.RequestDispatcher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class AuthenticationHandler implements InvocationHandler {

    public static final String AUTHENTICATE = "authenticate";

    private Authentication realHandler;

    public AuthenticationHandler(Authentication realHandler) {
        this.realHandler = realHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            Request request = RequestDispatcher.getCurrentRequest();
            SelectionKey selectionKey = RequestDispatcher.getCurrentSelectionKey();
            Object applicationCode = request.getParameters()[0];
            Object password = request.getParameters()[1];
            realHandler.authenticate(StringUtil.toString(applicationCode), StringUtil.toString(password));
            Map<String, Object> attrs = (Map<String, Object>) selectionKey.attachment();
            attrs.put(AUTHENTICATE, true);
            return null;
        } catch (Exception e) {
            throw new AuthenticationFailedException();
        }
    }
}
