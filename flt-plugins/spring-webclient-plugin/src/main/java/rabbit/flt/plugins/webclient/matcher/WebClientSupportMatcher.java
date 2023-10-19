package rabbit.flt.plugins.webclient.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class WebClientSupportMatcher extends SupportMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.web.reactive.function.client.DefaultWebClient$DefaultRequestBodyUriSpec");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("exchange");
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.webclient.plugin.WebClientSupportPlugin";
    }
}
