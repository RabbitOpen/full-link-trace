package rabbit.flt.plugins.webflux.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ServerHttpResponseMatcher extends SupportMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.http.server.reactive.AbstractServerHttpResponse");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return isPublic().and(named("setComplete"));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.webflux.plugin.ServerHttpResponsePlugin";
    }
}
