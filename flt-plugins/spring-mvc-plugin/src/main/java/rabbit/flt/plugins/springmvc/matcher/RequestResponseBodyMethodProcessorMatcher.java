package rabbit.flt.plugins.springmvc.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class RequestResponseBodyMethodProcessorMatcher implements SupportMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("handleReturnValue").and(takesArguments(4));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.springmvc.plugin.SpringMvcReturnValuePlugin";
    }
}
