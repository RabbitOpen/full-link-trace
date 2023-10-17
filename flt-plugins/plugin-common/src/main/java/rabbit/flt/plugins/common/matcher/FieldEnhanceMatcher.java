package rabbit.flt.plugins.common.matcher;

import rabbit.flt.plugins.common.Matcher;

/**
 * 字段扩展
 */
public abstract class FieldEnhanceMatcher extends Matcher {

    /**
     * 增强的字段名
     * @return
     */
    public abstract String getFiledName();

    /**
     * 字段类型
     * @return
     */
    public Class<?> getFieldTypeClass() {
        return Object.class;
    }

    /**
     * 字段访问接口
     * @return
     */
    public abstract Class<?> getAccessorClass();

    @Override
    public final String getPluginClassName() {
        return null;
    }
}
