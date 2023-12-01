package rabbit.flt.plugins.webclient.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * 匹配DefaultClientResponse， webclient 异常时写入body信息
 */
public class WebClientDefaultClientResponseMatcher implements SupportMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.web.reactive.function.client.DefaultClientResponse");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("body");
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.webclient.plugin.WebClientErrorBodyReaderPlugin";
    }
}
