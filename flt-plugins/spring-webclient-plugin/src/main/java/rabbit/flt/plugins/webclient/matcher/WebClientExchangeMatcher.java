package rabbit.flt.plugins.webclient.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class WebClientExchangeMatcher implements PerformanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.web.reactive.function.client.ExchangeFunctions$DefaultExchangeFunction");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("exchange");
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.webclient.plugin.WebClientExchangePlugin";
    }
}
