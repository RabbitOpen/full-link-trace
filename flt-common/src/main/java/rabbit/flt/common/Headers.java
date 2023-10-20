package rabbit.flt.common;

public interface Headers {

    // trace id在 http头中的名称
    String TRACE_ID = "openApiTraceId";

    // span id在 http头中的名称
    String SPAN_ID = "openApiSpanId";

    // 源app在 http头中的名称
    String SOURCE_APP = "openApiSourceApp";
}
