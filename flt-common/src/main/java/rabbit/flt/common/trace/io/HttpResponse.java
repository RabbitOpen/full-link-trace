package rabbit.flt.common.trace.io;

import rabbit.flt.common.trace.Output;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse extends Output {

    protected Map<String, Object> headers = new HashMap<>();

    /**
     * 响应码
     */
    protected int statusCode = 0;

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
