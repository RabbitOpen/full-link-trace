package rabbit.flt.plugins.springmvc.plugin;

import org.springframework.web.servlet.ModelAndView;
import rabbit.flt.common.Headers;
import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.MessageType;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpRequest;
import rabbit.flt.common.trace.io.HttpResponse;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Enumeration;

import static rabbit.flt.common.trace.TraceData.Status.ERR;

public class SpringMethodAdapterPlugin extends PerformancePlugin {

    /**
     * 非200响应时缓存traceData
     */
    private static final ThreadLocal<TraceData> errorContext = new ThreadLocal<>();

    @Override
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        if (DispatcherType.REQUEST == request.getDispatcherType()) {
            clearContext();
            TraceContext.clearContext();
            TraceContext.openTrace(method);
            String spanId = request.getHeader(Headers.SPAN_ID);
            if (StringUtils.isEmpty(spanId)) {
                TraceContext.initRootSpanId("0");
            } else {
                TraceContext.initRootSpanId(spanId.concat("-0"));
                String traceId = request.getHeader(Headers.TRACE_ID);
                if (!StringUtils.isEmpty(traceId)) {
                    TraceContext.setTraceId(traceId);
                }
            }
            super.before(objectEnhanced, method, args);
            TraceData traceData = TraceContext.getStackInfo(method).getTraceData();
            setSourceApplication(request, traceData);
            traceData.setNodeName("");
            TraceContext.setWebTraceDataContextData(traceData);
        }
        return args;
    }

    /**
     * 设置来源app字段到trace data中
     *
     * @param request
     * @param traceData
     */
    private void setSourceApplication(HttpServletRequest request, TraceData traceData) {
        String sourceApp = request.getHeader(Headers.SOURCE_APP);
        if (!StringUtils.isEmpty(sourceApp)) {
            traceData.setSourceApplication(StringUtils.toString(sourceApp));
        }
    }

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        if (DispatcherType.ERROR == request.getDispatcherType()) {
            return result;
        }
        return super.after(objectEnhanced, method, args, result);
    }

    @Override
    public void onException(Object objectEnhanced, Method method, Object[] args, Throwable t) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        if (DispatcherType.ERROR == request.getDispatcherType()) {
            return;
        }
        super.onException(objectEnhanced, method, args, t);
    }

    @Override
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        if (StringUtils.isEmpty(traceData.getNodeName())) {
            traceData.setNodeName(request.getRequestURI());
        } else {
            // 如果下游填充了path，则补全context path
            if (!"/".equals(request.getContextPath().trim())) {
                traceData.setNodeName(request.getContextPath().concat(traceData.getNodeName()));
            }
        }
        traceData.setNodeDesc(request.getMethod().concat(": ").concat(traceData.getNodeName()));
        traceData.setMessageType(MessageType.REST.name());
        setRequestInfo(args, traceData);
        setResponseInfo(args, traceData);
        // ！！！！正常响应的数据
        if (200 != traceData.getHttpResponse().getStatusCode()) {
            errorContext.set(traceData);
        } else {
            super.handleTraceData(traceData);
        }
    }

    /**
     * 设置请求参数
     *
     * @param args
     * @param traceData
     */
    private void setRequestInfo(Object[] args, TraceData traceData) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        HttpRequest requestInfo = new HttpRequest();
        requestInfo.setRequestUri(request.getRequestURI());
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            requestInfo.addHeader(name, truncate(request.getHeader(name)));
        }
        names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            requestInfo.addParameter(name, truncate(request.getParameter(name)));
        }
        traceData.setHttpRequest(requestInfo);
    }

    /**
     * 设置响应
     *
     * @param args
     * @param traceData
     */
    private void setResponseInfo(Object[] args, TraceData traceData) {
        HttpServletResponse response = (HttpServletResponse) args[1];
        HttpResponse responseInfo = new HttpResponse();
        try {
            if (ERR == traceData.getStatus()) {
                responseInfo.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                responseInfo.setStatusCode(response.getStatus());
            }
            for (String name : response.getHeaderNames()) {
                responseInfo.addHeader(name, truncate(response.getHeader(name)));
            }
            traceData.setHttpResponse(responseInfo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        HttpServletResponse response = (HttpServletResponse) args[1];
        if (DispatcherType.ERROR == request.getDispatcherType()) {
            TraceData traceData = errorContext.get();
            if (null != traceData) {
                ModelAndView view = (ModelAndView) result;
                HttpResponse httpResponse = getHttpResponse(traceData);
                if (null != view) {
                    httpResponse.setBody(truncate(StringUtils.toString(view.getModelMap())));
                }
                httpResponse.setStatusCode(500);
                for (String name : response.getHeaderNames()) {
                    httpResponse.addHeader(name, truncate(response.getHeader(name)));
                }
                clearContext();
                traceData.setCost(System.currentTimeMillis() - traceData.getRequestTime());
                super.handleTraceData(traceData);
            }
            return;
        }
        super.doFinal(objectEnhanced, method, args, result);
    }

    private HttpResponse getHttpResponse(TraceData traceData) {
        if (null == traceData.getHttpResponse()) {
            traceData.setHttpResponse(new HttpResponse());
        }
        return traceData.getHttpResponse();
    }

    @Override
    protected void handleTraceData(TraceData traceData) {
        // do nothing, 放在填充数据阶段发送trace
    }

    public static TraceData getTraceContext() {
        return errorContext.get();
    }

    public static void clearContext() {
        errorContext.remove();
    }
}
