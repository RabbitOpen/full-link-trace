package rabbit.flt.common.trace;

import rabbit.flt.common.trace.io.HttpRequest;
import rabbit.flt.common.trace.io.HttpResponse;

import static rabbit.flt.common.trace.TraceData.Status.OK;

public class TraceData {

    private String traceId;

    // trace 节点名
    private String nodeName;

    /**
     * 节点描述
     */
    private String nodeDesc;

    private String spanId;

    /**
     * 请求时间
     */
    private Long requestTime;

    /**
     * 当前节点ip
     */
    private String serverIp;

    // 应用编码
    private String applicationCode;

    private String sourceApplication;

    // 耗时
    private Long cost;

    private Status status = OK;

    public enum Status {
        OK, ERR
    }

    /**
     * 输入
     */
    private HttpRequest httpRequest;

    /**
     * 输出
     */
    private HttpResponse httpResponse;

    /**
     * 消息类型
     */
    private String messageType = MessageType.METHOD.name();

    /**
     * 附加数据
     */
    private String data;

    /**
     * 故障点（触发异常的那个函数）
     */
    private boolean exceptionPoint = false;

    /**
     * 是否有controller接口承接请求
     */
    private boolean hasMappedController = false;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeDesc() {
        return nodeDesc;
    }

    public void setNodeDesc(String nodeDesc) {
        this.nodeDesc = nodeDesc;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getSourceApplication() {
        return sourceApplication;
    }

    public void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isExceptionPoint() {
        return exceptionPoint;
    }

    public void setExceptionPoint(boolean exceptionPoint) {
        this.exceptionPoint = exceptionPoint;
    }

    public boolean isHasMappedController() {
        return hasMappedController;
    }

    public void setHasMappedController(boolean hasMappedController) {
        this.hasMappedController = hasMappedController;
    }
}
