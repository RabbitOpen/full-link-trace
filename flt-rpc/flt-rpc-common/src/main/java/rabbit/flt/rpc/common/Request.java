package rabbit.flt.rpc.common;

public class Request {

    /**
     * 函数名
     */
    private String methodName;

    /**
     * 所属接口
     */
    private Class<?> interfaceClz;

    /**
     * 入参
     */
    private Object[] parameters;

    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?> getInterfaceClz() {
        return interfaceClz;
    }

    public void setInterfaceClz(Class<?> interfaceClz) {
        this.interfaceClz = interfaceClz;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
