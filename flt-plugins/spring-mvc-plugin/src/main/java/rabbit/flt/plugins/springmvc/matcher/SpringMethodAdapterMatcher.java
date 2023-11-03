package rabbit.flt.plugins.springmvc.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class SpringMethodAdapterMatcher implements PerformanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("handle").and(takesArguments(3));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.springmvc.plugin.SpringMethodAdapterPlugin";
    }
}
