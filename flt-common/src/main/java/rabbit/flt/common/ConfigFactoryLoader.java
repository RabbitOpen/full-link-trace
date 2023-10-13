package rabbit.flt.common;

/**
 * 工厂加载器
 */
public interface ConfigFactoryLoader {

    /**
     * 工厂加载器
     * @return
     */
    AbstractConfigFactory loadFactory();
}
