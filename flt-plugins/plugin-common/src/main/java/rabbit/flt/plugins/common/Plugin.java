package rabbit.flt.plugins.common;

import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.trace.MethodStackInfo;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.utils.ResourceUtils;
import rabbit.flt.common.utils.StringUtils;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

/**
 * 插件
 */
public abstract class Plugin {

    protected Logger logger = AgentLoggerFactory.getLogger(getClass());

    /**
     * trace数据处理器
     */
    protected static final DefaultTraceDataHandler dataHandler = new DefaultTraceDataHandler();

    /**
     * 方法前置增强
     *
     * @param objectEnhanced
     * @param method
     * @param args
     * @return 返回入参
     * @throws Exception
     */
    public Object[] before(Object objectEnhanced, Method method, Object[] args) {
        TraceContext.pushStack(method);
        return args;
    }

    protected Class<?> loadClass(String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }

    /**
     * 后置拦截
     *
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     * @return
     */
    public Object after(Object objectEnhanced, Method method, Object[] args, Object result) {
        MethodStackInfo stackInfo = TraceContext.getStackInfo(method);
        if (null == stackInfo) {
            return result;
        }
        TraceContext.clearError();
        return result;
    }

    /**
     * final增强
     *
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     */
    public void doFinal(Object objectEnhanced, Method method, Object[] args, Object result) throws Exception {
        MethodStackInfo stackInfo = TraceContext.popStack(method);
        if (null == stackInfo) {
            return;
        }
        TraceData traceData = stackInfo.getTraceData();
        fillTraceData(traceData, objectEnhanced, method, args, result);
        if (stackInfo.isPopped()) {
            handleTraceData(traceData);
        }
    }

    /**
     * 异常兜底
     *
     * @param objectEnhanced
     * @param method
     * @param args
     * @param t
     */
    public void onException(Object objectEnhanced, Method method, Object[] args, Throwable t) {
        MethodStackInfo stackInfo = TraceContext.getStackInfo(method);
        if (null == stackInfo) {
            return;
        }
        TraceData traceData = stackInfo.getTraceData();
        traceData.setExceptionPoint(false);
        traceData.setStatus(TraceData.Status.ERR);
        if (!TraceContext.isErrorNode()) {
            traceData.setData(getStackInfo(t));
            traceData.setExceptionPoint(true);
            TraceContext.flagError();
        }
    }

    /**
     * 读取堆栈文本
     *
     * @param t
     * @return
     */
    private String getStackInfo(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        t.printStackTrace(pw);
        String text = writer.toString();
        BufferedReader br = new BufferedReader(new StringReader(text));
        StringBuilder sb = new StringBuilder();
        int lineCount = 0;
        while (true) {
            try {
                String line = br.readLine();
                sb.append(null == line ? "" : line).append("\r");
                if (null == line || 30 == ++lineCount) {
                    break;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        ResourceUtils.close(writer);
        ResourceUtils.close(pw);
        ResourceUtils.close(br);
        return sb.toString();
    }

    protected void handleTraceData(TraceData traceData) {
        dataHandler.process(traceData);
    }

    /**
     * 填充trace数据
     *
     * @param traceData
     * @param objectEnhanced
     * @param method
     * @param args
     * @param result
     */
    protected void fillTraceData(TraceData traceData, Object objectEnhanced, Method method, Object[] args, Object result) throws Exception {

    }

    protected boolean isTraceOpened() {
        return TraceContext.isTraceOpened();
    }

    /**
     * 截断数据
     *
     * @param txt
     * @return
     */
    protected String truncate(String txt) {
        if (!StringUtils.isEmpty(txt) && txt.length() > 512) {
            return txt.substring(0, 512) + ".....";
        }
        return txt;
    }
}
