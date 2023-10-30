package rabbit.flt.common;

public final class Headers {

    private Headers() {}

    // trace id在 http头中的名称
    public static final String TRACE_ID = "openApiTraceId";

    // span id在 http头中的名称
    public static final String SPAN_ID = "openApiSpanId";

    // 源app在 http头中的名称
    public static final String SOURCE_APP = "openApiSourceApp";
}
