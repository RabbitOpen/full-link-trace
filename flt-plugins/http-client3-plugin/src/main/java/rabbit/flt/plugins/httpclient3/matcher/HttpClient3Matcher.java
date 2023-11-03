package rabbit.flt.plugins.httpclient3.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class HttpClient3Matcher implements PerformanceMatcher {
    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.apache.commons.httpclient.HttpClient");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("executeMethod").and(takesArguments(3));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.httpclient3.plugin.HttpClient3Plugin";
    }
}
