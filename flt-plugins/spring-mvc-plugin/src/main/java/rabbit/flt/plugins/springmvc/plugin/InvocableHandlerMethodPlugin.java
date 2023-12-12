package rabbit.flt.plugins.springmvc.plugin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpResponse;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.SupportPlugin;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static javax.servlet.DispatcherType.ERROR;

public class InvocableHandlerMethodPlugin extends SupportPlugin {

    @Override
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        NativeWebRequest request = (NativeWebRequest) args[0];
        if (request.getNativeRequest() instanceof HttpServletRequest &&
               ERROR == ((HttpServletRequest) request.getNativeRequest()).getDispatcherType()) {
            TraceData traceData = SpringMethodAdapterPlugin.getTraceContext();
            if (null != traceData) {
                if (null == traceData.getHttpResponse()) {
                    traceData.setHttpResponse(new HttpResponse());
                }
                String content = StringUtils.toString(result);
                if (result instanceof ResponseEntity) {
                    content = StringUtils.toString(((ResponseEntity<?>) result).getBody());
                }
                traceData.getHttpResponse().setBody(truncate(content));
            }
        }
        return result;
    }
}
