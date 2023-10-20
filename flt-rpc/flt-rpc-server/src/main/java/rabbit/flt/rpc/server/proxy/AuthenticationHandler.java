package rabbit.flt.rpc.server.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.rpc.common.Request;
import rabbit.flt.rpc.common.exception.AuthenticationFailedException;
import rabbit.flt.rpc.common.rpc.Authentication;
import rabbit.flt.rpc.server.RequestDispatcher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class AuthenticationHandler implements InvocationHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String AUTHENTICATE = "authenticate";

    private Authentication realHandler;

    public AuthenticationHandler(Authentication realHandler) {
        this.realHandler = realHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Request request = RequestDispatcher.getCurrentRequest();
            SelectionKey selectionKey = RequestDispatcher.getCurrentSelectionKey();
            Object applicationCode = request.getParameters()[0];
            Object password = request.getParameters()[1];
            boolean result = realHandler.authenticate(StringUtils.toString(applicationCode), StringUtils.toString(password));
            Map<String, Object> attrs = (Map<String, Object>) selectionKey.attachment();
            attrs.put(AUTHENTICATE, result);
            return result;
        } catch (Exception e) {
            throw new AuthenticationFailedException();
        }
    }
}
