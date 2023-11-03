package rabbit.flt.plugins.common;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 匹配定义
 */
public interface Matcher {

    /**
     * 定义哪些class可以被增强
     *
     * @return
     */
    ElementMatcher.Junction<TypeDescription> classMatcher();

    /**
     * 声明哪些方法可以被增强
     *
     * @param typeDescription
     * @return
     */
    ElementMatcher.Junction methodMatcher(TypeDescription typeDescription);

    /**
     * 插件class名
     *
     * @return
     */
    String getPluginClassName();

    /**
     * 获取已经定义的matcher
     *
     * @return
     */
    static List<Matcher> loadMatchers() {
        List<Matcher> matchers = new ArrayList<>();
        ServiceLoader<Matcher> loader = ServiceLoader.load(Matcher.class);
        for (Matcher matcher : loader) {
            matchers.add(matcher);
        }
        return matchers;
    }
}
