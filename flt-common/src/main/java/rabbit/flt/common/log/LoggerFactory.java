package rabbit.flt.common.log;

public interface LoggerFactory {

    /**
     * 获取日志对象
     * @param name
     * @return
     */
    Logger getLogger(String name);

    /**
     * 获取日志对象
     * @param clz
     * @return
     */
    Logger getLogger(Class<?> clz);

}
