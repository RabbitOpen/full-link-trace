package rabbit.flt.plugins.reactor.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.ConstructorMatcher;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ReactorConstructorEnhanceMatcher extends ConstructorMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("reactor.core.publisher.Mono").or(named("reactor.core.publisher.Flux"));
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return any();
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.reactor.plugin.ReactorConstructorPlugin";
    }
}
