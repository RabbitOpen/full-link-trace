package rabbit.flt.plugins.mybatis.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * tk mybatis， mybatis plus 公共插件
 */
public class MappedStatementSupportMatcher extends SupportMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.apache.ibatis.mapping.MappedStatement");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("getBoundSql");
    }

    @Override
    public String getPluginClassName() {
        return null;
    }
}
