package rabbit.flt.plugins.mybatis.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class TkMybatisMatcher implements PerformanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.apache.ibatis.binding.MapperProxy");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("invoke");
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.mybatis.plugin.MapperProxyPlugin";
    }
}
