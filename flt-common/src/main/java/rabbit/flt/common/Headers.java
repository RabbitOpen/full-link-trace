package rabbit.flt.common;

public final class Headers {

    private Headers() {}

    // trace id在 http头中的名称
    public static final String TRACE_ID = "fltTraceId";

    // span id在 http头中的名称
    public static final String SPAN_ID = "fltSpanId";

    // 源app在 http头中的名称
    public static final String SOURCE_APP = "fltSourceApp";
}
