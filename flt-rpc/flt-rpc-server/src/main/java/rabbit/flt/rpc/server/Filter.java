package rabbit.flt.rpc.server;

/**
 * 服务端请求拦截器
 */
public interface Filter {

    /**
     * 请求拦截
     *
     * @param chain
     */
    void doFilter(FilterChain chain) ;

    /**
     * 优先级，值越小，优先级越高
     * @return
     */
    default Integer getPriority() {
        return Integer.MAX_VALUE;
    }
}
