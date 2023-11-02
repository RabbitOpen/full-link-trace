package rabbit.flt.core.transformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.utility.JavaModule;
import rabbit.flt.common.spi.ClassProxyListener;
import rabbit.flt.core.callback.MethodCallback;
import rabbit.flt.core.interceptor.ConstructorInterceptor;
import rabbit.flt.core.interceptor.MethodInterceptor;
import rabbit.flt.plugins.common.Matcher;
import rabbit.flt.plugins.common.matcher.ConstructorMatcher;
import rabbit.flt.plugins.common.matcher.FieldEnhanceMatcher;
import rabbit.flt.plugins.common.matcher.PerformanceMatcher;
import rabbit.flt.plugins.common.matcher.SupportMatcher;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * 增强器
 */
public class ClassTransformer implements AgentBuilder.Transformer {

    private List<Matcher> supportMatchers;

    private List<Matcher> performanceMatchers;

    private List<Matcher> constructorMatchers;

    private List<Matcher> fieldEnhanceMatchers;

    private List<ClassProxyListener> listeners = new ArrayList<>();

    public ClassTransformer(List<Matcher> matchers) {
        for (ClassProxyListener listener : ServiceLoader.load(ClassProxyListener.class)) {
            listeners.add(listener);
        }
        supportMatchers = matchers.stream().filter(matcher -> matcher instanceof SupportMatcher).collect(Collectors.toList());
        performanceMatchers = matchers.stream().filter(matcher -> matcher instanceof PerformanceMatcher).collect(Collectors.toList());
        constructorMatchers = matchers.stream().filter(matcher -> matcher instanceof ConstructorMatcher).collect(Collectors.toList());
        fieldEnhanceMatchers = matchers.stream().filter(matcher -> matcher instanceof FieldEnhanceMatcher).collect(Collectors.toList());
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
        DynamicType.Builder<?> typeBuilder = builder;
        listeners.forEach(l -> l.onProxy(typeDescription.getCanonicalName()));
        for (Matcher matcher : fieldEnhanceMatchers) {
            if (matcher.classMatcher().matches(typeDescription)) {
                FieldEnhanceMatcher fm = (FieldEnhanceMatcher) matcher;
                typeBuilder = typeBuilder.defineField(fm.getFiledName(), fm.getFieldTypeClass(), Modifier.PRIVATE)
                        .implement(fm.getAccessorClass())
                        .intercept(FieldAccessor.ofBeanProperty());
            }
        }

        for (Matcher matcher : constructorMatchers) {
            if (matcher.classMatcher().matches(typeDescription)) {
                typeBuilder = typeBuilder.constructor(matcher.methodMatcher(typeDescription))
                        .intercept(SuperMethodCall.INSTANCE.andThen((MethodDelegation.withDefaultConfiguration()
                                .to(new ConstructorInterceptor(matcher.getPluginClassName())))));
            }
        }

        boolean support = false;
        for (Matcher matcher : supportMatchers) {
            if (matcher.classMatcher().matches(typeDescription)) {
                typeBuilder = typeBuilder.method(matcher.methodMatcher(typeDescription))
                        .intercept(MethodDelegation.withDefaultConfiguration()
                                .withBinders(Morph.Binder.install(MethodCallback.class))
                                .to(new MethodInterceptor(matcher.getPluginClassName())));
                support = true;
            }
        }
        if (support) {
            return typeBuilder;
        }

        for (Matcher matcher : performanceMatchers) {
            if (matcher.classMatcher().matches(typeDescription)) {
                typeBuilder = typeBuilder.method(matcher.methodMatcher(typeDescription))
                        .intercept(MethodDelegation.withDefaultConfiguration()
                                .withBinders(Morph.Binder.install(MethodCallback.class))
                                .to(new MethodInterceptor(matcher.getPluginClassName())));
            }
        }
        return typeBuilder;
    }
}
