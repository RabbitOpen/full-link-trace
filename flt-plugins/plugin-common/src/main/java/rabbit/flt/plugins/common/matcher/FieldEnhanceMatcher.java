package rabbit.flt.plugins.common.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.Matcher;

/**
 * 字段扩展
 */
public abstract class FieldEnhanceMatcher implements Matcher {

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

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        throw new UnsupportedOperationException();
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
