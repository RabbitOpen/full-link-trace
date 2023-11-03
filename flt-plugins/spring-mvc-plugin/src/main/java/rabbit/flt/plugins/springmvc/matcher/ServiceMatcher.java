package rabbit.flt.plugins.springmvc.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.common.Traceable;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ServiceMatcher implements PerformanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return hasAnnotation(annotationType(named("org.springframework.stereotype.Service")))
                .and(not(isInterface()))
                // 不增强自带的Component
                .and(not(nameStartsWith("org.springframework")));
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return isPublic().and(not(named("toString").or(named("hashCode")).or(named("equal"))))
                .and(not(isAnnotatedWith(Traceable.class)));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.common.plugin.PerformancePlugin";
    }
}
