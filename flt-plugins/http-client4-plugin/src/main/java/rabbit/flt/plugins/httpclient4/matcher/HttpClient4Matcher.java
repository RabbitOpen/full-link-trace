package rabbit.flt.plugins.httpclient4.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class HttpClient4Matcher extends PerformanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.apache.http.impl.client.AbstractHttpClient")
                .or(named("org.apache.http.impl.client.MinimalHttpClient"))
                .or(named("org.apache.http.impl.client.InternalHttpClient"));
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("doExecute");
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.httpclient4.plugin.HttpClient4Plugin";
    }
}
