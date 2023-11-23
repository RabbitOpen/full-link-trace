package rabbit.flt.plugins.logback.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isProtected;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class LogbackMatcher implements SupportMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("ch.qos.logback.core.pattern.PatternLayoutBase");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return isProtected().and(named("writeLoopOnConverters"));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.logback.plugin.LogbackPlugin";
    }
}
