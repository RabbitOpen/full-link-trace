package rabbit.flt.plugins.springmvc.matcher;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.common.Traceable;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ControllerMatcher extends PerformanceMatcher {

    @Override
    public ElementMatcher.Junction<TypeDescription> classMatcher() {
        return getClassDescription().or(declaresMethod(isAnnotatedWith(getMethodDescription()))
                .and(not(isInterface())));
    }

    /**
     * 匹配的controller
     * @return
     */
    private ElementMatcher.Junction<TypeDescription> getClassDescription() {
        String restController = "org.springframework.web.bind.annotation.RestController";
        String controller = "org.springframework.stereotype.Controller";
        return not(isInterface()).and(hasAnnotation(annotationType(named(restController))
                .or(annotationType(named(controller)))))
                // 不增强自带的Component
                .and(not(nameStartsWith("org.springframework")));
    }

    /**
     * 匹配的方法
     * @return
     */
    private ElementMatcher.Junction getMethodDescription() {
        String postMapping = "org.springframework.web.bind.annotation.PostMapping";
        String getMapping = "org.springframework.web.bind.annotation.GetMapping";
        String requestMapping = "org.springframework.web.bind.annotation.RequestMapping";
        String deleteMapping = "org.springframework.web.bind.annotation.DeleteMapping";
        String putMapping = "org.springframework.web.bind.annotation.PutMapping";
        String patchMapping = "org.springframework.web.bind.annotation.PatchMapping";
        return isAnnotatedWith(named(requestMapping))
                .or(isAnnotatedWith(named(postMapping)))
                .or(isAnnotatedWith(named(getMapping)))
                .or(isAnnotatedWith(named(deleteMapping)))
                .or(isAnnotatedWith(named(putMapping)))
                .or(isAnnotatedWith(named(patchMapping)));
    }

    @Override
    public ElementMatcher.Junction methodMatcher(TypeDescription typeDescription) {
        return super.methodMatcher(typeDescription).and(not(isAnnotatedWith(Traceable.class)));
    }

    @Override
    public String getPluginClassName() {
        return "rabbit.flt.plugins.springmvc.plugin.ControllerPlugin";
    }
}
