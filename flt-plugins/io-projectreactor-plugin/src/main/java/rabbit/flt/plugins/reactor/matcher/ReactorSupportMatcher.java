package rabbit.flt.plugins.reactor.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class ReactorSupportMatcher implements SupportMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("reactor.core.publisher.Mono").or(named("reactor.core.publisher.Flux"));
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("subscribe").or(named("subscribeWith"));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.reactor.plugin.ReactorSupportPlugin";
    }
}
