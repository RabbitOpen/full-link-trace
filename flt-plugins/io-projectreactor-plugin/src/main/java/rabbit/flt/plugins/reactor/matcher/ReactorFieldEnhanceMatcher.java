package rabbit.flt.plugins.reactor.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.plugins.common.matcher.FieldEnhanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class ReactorFieldEnhanceMatcher extends FieldEnhanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("reactor.core.publisher.Mono").or(named("reactor.core.publisher.Flux"))
                .or(named("org.springframework.web.reactive.function.client.DefaultClientRequestBuilder$BodyInserterRequest"));
    }

    @Override
    public String getFiledName() {
        return "traceContextData";
    }

    @Override
    public Class<?> getAccessorClass() {
        return TraceContextHolder.class;
    }
}
