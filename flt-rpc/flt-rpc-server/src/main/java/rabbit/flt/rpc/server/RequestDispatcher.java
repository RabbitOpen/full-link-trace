package rabbit.flt.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.rpc.common.*;
import rabbit.flt.rpc.common.exception.AuthenticationFailedException;
import rabbit.flt.rpc.common.exception.UnAuthenticatedException;
import rabbit.flt.rpc.common.rpc.Authentication;
import rabbit.flt.rpc.common.rpc.RpcRequest;
import rabbit.flt.rpc.common.rpc.RpcResponse;
import rabbit.flt.rpc.server.proxy.AuthenticationHandler;
import rabbit.flt.rpc.server.proxy.RpcRequestHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class RequestDispatcher implements Registrar {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final ThreadLocal<SelectionKey> keyHolder = new ThreadLocal<>();

    public static final ThreadLocal<Request> requestHolder = new ThreadLocal<>();

    /**
     * 处理器缓存
     */
    protected Map<Class<?>, Object> handlerCache = new ConcurrentHashMap<>();

    /**
     * 处理请求
     *
     * @param key
     * @param rpcRequest
     */
    public void handleRequest(SelectionKey key, RpcRequest rpcRequest) {
        Request request = rpcRequest.getRequest();
        RpcResponse<Object> response = new RpcResponse<>();
        response.setRequestId(rpcRequest.getRequestId());
        try {
            keyHolder.set(key);
            requestHolder.set(request);
            callBizMethod(request, response);
            response.setSuccess(true);
        } catch (UnAuthenticatedException | AuthenticationFailedException e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.UN_AUTHENTICATED);
            response.setMsg(e.getMessage());
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            response.setSuccess(false);
            response.setMsg(e.getMessage());
            response.setCode(ResponseCode.FAILED);
        } finally {
            keyHolder.remove();
            requestHolder.remove();
        }
        write(key, response);
    }

    /**
     * 调业务方法
     *
     * @param request
     * @param response
     * @throws Throwable
     */
    private void callBizMethod(Request request, RpcResponse response) throws Throwable {
        try {
            Method method = request.getInterfaceClz().getDeclaredMethod(request.getMethodName(), request.getParameterTypes());
            Object handler = this.handlerCache.get(request.getInterfaceClz());
            response.setData(method.invoke(handler, request.getParameters()));
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RpcException) {
                throw e.getCause();
            }
            throw e.getTargetException();
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
        boolean compress = false;
        int originalSize = bytes.length;
        if (originalSize > 1024 * 256) {
            bytes = GzipUtil.compress(bytes);
            compress = true;
        }
        try {
            lock.lock();
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(12 + bytes.length);
            buffer.putInt(bytes.length);
            buffer.putInt(compress ? DataType.GZIPPED : DataType.UN_COMPRESSED);
            buffer.putInt(originalSize);
            buffer.put(bytes);
            buffer.position(0);
            while (buffer.position() != buffer.capacity()) {
                if (0 == channel.write(buffer)) {
                    LockSupport.park(2L * 1000 * 1000);
                }
            }
        } catch (Exception e) {
            logger.error("response[{}] error: {}", data.getRequestId(), e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 注册处理器
     * @param clz
     * @param handler
     * @param <T>
     */
    @Override
    public <T> void register(Class<T> clz, T handler) {
        InvocationHandler proxyHandler;
        if (Authentication.class == clz) {
            proxyHandler = new AuthenticationHandler((Authentication) handler);
        } else {
            proxyHandler = new RpcRequestHandler(handler);
        }
        registerHandler(clz, Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, proxyHandler));
    }

    public Object getHandler(Class<?> clz) {
        return handlerCache.get(clz);
    }

    protected <T> void registerHandler(Class<T> clz, Object proxyHandler) {
        this.handlerCache.put(clz, proxyHandler);
    }

    public static Request getCurrentRequest() {
        return requestHolder.get();
    }

    public static SelectionKey getCurrentSelectionKey() {
        return keyHolder.get();
    }
}
