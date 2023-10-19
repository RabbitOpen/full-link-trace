package rabbit.flt.plugins.webflux.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.common.TraceContextHolder;
import rabbit.flt.plugins.common.matcher.FieldEnhanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * 增强response，注入 traceData数据
 */
public class ResponseFieldEnhanceMatcher extends FieldEnhanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return named("org.springframework.http.server.reactive.AbstractServerHttpResponse");
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
