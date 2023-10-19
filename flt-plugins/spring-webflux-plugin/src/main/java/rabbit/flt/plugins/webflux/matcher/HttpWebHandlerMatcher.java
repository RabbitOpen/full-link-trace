package rabbit.flt.plugins.webflux.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class HttpWebHandlerMatcher extends PerformanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.web.server.adapter.HttpWebHandlerAdapter");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return isPublic().and(named("handle")).and(takesArguments(2));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.webflux.plugin.SpringWebFluxPlugin";
    }
}
