package rabbit.flt.common.trace.io;

import rabbit.flt.common.trace.Input;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest implements Input {

    protected Map<String, Object> headers = new HashMap<>();

    protected Map<String, Object> requestParameters = new HashMap<>();

    // 请求路径
    private String requestUri;

    private String method;

    private String contextPath;

    public HttpRequest() {
    }

    public HttpRequest(String requestUri) {
        this.requestUri = requestUri;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addParameter(String name, String value) {
        requestParameters.put(name, value);
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(Map<String, Object> requestParameters) {
        this.requestParameters = requestParameters;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
