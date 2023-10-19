package rabbit.flt.plugins.logback.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class LogbackLineNumberMatcher extends SupportMatcher {
    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("ch.qos.logback.classic.pattern.LineOfCallerConverter");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return isPublic().and(named("convert"));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.logback.plugin.LineNumberPlugin";
    }
}
