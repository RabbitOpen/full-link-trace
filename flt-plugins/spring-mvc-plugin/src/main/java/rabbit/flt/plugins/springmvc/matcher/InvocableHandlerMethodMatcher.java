package rabbit.flt.plugins.springmvc.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * dispatch type 为error时 读取返回的异常body
 */
public class InvocableHandlerMethodMatcher implements SupportMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.web.method.support.InvocableHandlerMethod");
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return named("invokeForRequest");
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.springmvc.plugin.InvocableHandlerMethodPlugin";
    }
}
