package rabbit.flt.plugins.traceable.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.common.Traceable;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class TraceableMatcher implements PerformanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return declaresMethod(isAnnotatedWith(Traceable.class))
                .and(not(isInterface()));
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return isAnnotatedWith(Traceable.class);
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.traceable.plugin.TraceablePlugin";
    }
}
