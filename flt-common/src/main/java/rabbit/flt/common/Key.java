package rabbit.flt.common;

public interface Key {

    // trace id在 http头中的名称
    String traceIdHeaderName = "openApiTraceId";

    // span id在 http头中的名称
    String spanIdHeaderName = "openApiSpanId";

    // 源app在 http头中的名称
    String sourceAppHeaderName = "openApiSourceApp";
}
