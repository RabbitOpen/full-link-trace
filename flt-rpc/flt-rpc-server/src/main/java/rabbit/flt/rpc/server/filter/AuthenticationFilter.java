package rabbit.flt.rpc.server.filter;

import rabbit.flt.rpc.common.Request;
import rabbit.flt.rpc.common.exception.AuthenticationException;
import rabbit.flt.rpc.common.exception.AuthenticationFailedException;
import rabbit.flt.rpc.common.rpc.Authentication;
import rabbit.flt.rpc.common.rpc.KeepAlive;
import rabbit.flt.rpc.server.Filter;
import rabbit.flt.rpc.server.FilterChain;

import java.util.Map;

public class AuthenticationFilter implements Filter {

    private static final String AUTHENTICATE = "AUTHENTICATION_FLAG";

    @Override
    public void doFilter(FilterChain filterChain) {
        Request request = filterChain.getRequest();
        Map<String, Object> attrs = (Map<String, Object>) filterChain.getSelectionKey().attachment();
        if (attrs.containsKey(AUTHENTICATE)) {
            filterChain.doChain();
        } else {
            if (KeepAlive.class == request.getInterfaceClz()) {
                // 心跳忽略认证
                filterChain.doChain();
            } else if (Authentication.class == request.getInterfaceClz()) {
                try {
                    filterChain.doChain();
                    attrs.put(AUTHENTICATE, true);
                } catch (Exception e) {
                    throw new AuthenticationFailedException(e);
                }
            } else {
                // 没有认证就发起请求
                throw new AuthenticationException();
            }
        }
    }

    @Override
    public Integer getPriority() {
        return 0;
    }
}
