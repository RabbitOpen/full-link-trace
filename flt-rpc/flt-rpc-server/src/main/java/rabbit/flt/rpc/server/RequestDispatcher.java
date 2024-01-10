package rabbit.flt.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.common.utils.GZipUtils;
import rabbit.flt.common.utils.ReflectUtils;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.rpc.common.*;
import rabbit.flt.rpc.common.exception.AuthenticationException;
import rabbit.flt.rpc.common.rpc.RpcRequest;
import rabbit.flt.rpc.common.rpc.RpcResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static rabbit.flt.rpc.server.Server.SELECTION_KEY;

public class RequestDispatcher implements Registrar {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static boolean hasMono;

    static {
        try {
            Class.forName("reactor.core.publisher.Mono");
            hasMono = true;
        } catch (ClassNotFoundException e) {
            hasMono = false;
        }
    }

    /**
     * 处理器缓存
     */
    protected Map<Class<?>, Object> handlerCache = new ConcurrentHashMap<>();

    /**
     * 处理请求
     *
     * @param key
     * @param rpcRequest
     * @param filterChain
     */
    public void handleRequest(SelectionKey key, RpcRequest rpcRequest, FilterChain filterChain) {
        Request request = rpcRequest.getRequest();
        RpcResponse<Object> response = new RpcResponse<>();
        response.setRequestId(rpcRequest.getRequestId());
        try {
            FilterChain chain = new FilterChain(filterChain.getFilters(), request, key);
            chain.add(c -> {
                Object result = callBizMethod(c.getRequest());
                if (hasMono && (result instanceof Mono)) {
                    handleMonoResponse(key, response, (Mono) result);
                } else {
                    response.setData(result);
                    response.setSuccess(true);
                    write(key, response);
                }
                // 后置过滤器
                c.doChain();
            });
            chain.doChain();
        } catch (AuthenticationException e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.UN_AUTHENTICATED);
            response.setMsg(e.getMessage());
            write(key, response);
        } catch (Exception e) {
            responseWhenError(key, response, e);
        }
    }

    /**
     * 处理异步响应
     *
     * @param key
     * @param response
     * @param result
     */
    private void handleMonoResponse(SelectionKey key, RpcResponse<Object> response, Mono<Object> result) {
        result.map(d -> {
            response.setData(d);
            response.setSuccess(true);
            write(key, response);
            return d;
        }).switchIfEmpty(Mono.defer(() -> {
            response.setSuccess(true);
            write(key, response);
            return Mono.empty();
        })).onErrorResume(e -> {
            responseWhenError(key, response, e);
            return Mono.empty();
        }).contextWrite(ctx -> ctx.put(SELECTION_KEY, key)).subscribe();
    }

    private void responseWhenError(SelectionKey key, RpcResponse<Object> response, Throwable t) {
        logger.warn(t.getMessage());
        response.setSuccess(false);
        response.setMsg(t.getMessage());
        response.setCode(ResponseCode.FAILED);
        write(key, response);
    }

    /**
     * 调业务方法
     *
     * @param request
     * @throws Throwable
     */
    private Object callBizMethod(Request request) {
        try {
            Class clz = request.getInterfaceClz();
            if (!StringUtils.isEmpty(request.getHandlerInterfaceName())) {
                clz = ReflectUtils.loadClass(request.getHandlerInterfaceName());
            }
            Method method = clz.getDeclaredMethod(request.getMethodName(), request.getParameterTypes());
            Object handler = this.handlerCache.get(clz);
            if (null == handler) {
                throw new RpcException(String.format("no handler for clz[%s]", clz.getName()));
            }
            return method.invoke(handler, request.getParameters());
        } catch (Exception e) {
            throw new RpcException(e.getCause().getMessage());
        }
    }

    /**
     * 输出数据
     *
     * @param key
     * @param data
     */
    protected void write(SelectionKey key, RpcResponse data) {
        Map<String, Object> attrs = (Map<String, Object>) key.attachment();
        ReentrantLock lock = (ReentrantLock) attrs.get(Attributes.WRITE_LOCK);
        byte[] bytes = Serializer.serialize(data);
        boolean zipped = false;
        int originalSize = bytes.length;
        if (originalSize > 1024 * 256) {
            bytes = GZipUtils.zip(bytes);
            zipped = true;
        }
        try {
            lock.lock();
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(12 + bytes.length);
            buffer.putInt(bytes.length);
            buffer.putInt(zipped ? DataType.GZIPPED : DataType.UN_COMPRESSED);
            buffer.putInt(originalSize);
            buffer.put(bytes);
            buffer.position(0);
            while (buffer.position() != buffer.capacity()) {
                if (0 == channel.write(buffer)) {
                    LockSupport.park(2L * 1000 * 1000);
                }
            }
        } catch (ClosedChannelException e) {
            logger.warn("response[{}] error, client channel is closed!", data.getRequestId());
        } catch (Exception e) {
            logger.warn("response[{}] error: {}", data.getRequestId(), e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 注册处理器
     *
     * @param clz
     * @param handler
     * @param <T>
     */
    @Override
    public <T> void register(Class<T> clz, T handler) {
        this.handlerCache.put(clz, handler);
    }

    public Object getHandler(Class<?> clz) {
        return handlerCache.get(clz);
    }

}
