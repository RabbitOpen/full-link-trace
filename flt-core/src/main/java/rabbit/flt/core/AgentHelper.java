package rabbit.flt.core;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.core.loader.LocalPluginClassLoader;
import rabbit.flt.core.transformer.ClassTransformer;
import rabbit.flt.plugins.common.Matcher;

import java.lang.instrument.Instrumentation;
import java.util.List;

public class AgentHelper {

    /**
     * 安装插件
     *
     * @param instrumentation
     */
    public static void installPlugins(Instrumentation instrumentation) {
        PluginClassLoader.setPluginClassLoader(new LocalPluginClassLoader());
        doInstall(instrumentation);
    }

    /**
     * 安装插件
     */
    public static void installPlugins() {
        ByteBuddyAgent.install();
        installPlugins(ByteBuddyAgent.getInstrumentation());
    }

    public static void doInstall(Instrumentation instrumentation) {
        List<Matcher> matchers = Matcher.loadMatchers();
        ElementMatcher.Junction<TypeDescription> classMatcher = null;
        for (Matcher matcher : matchers) {
            if (null == classMatcher) {
                classMatcher = matcher.classMatcher();
            } else {
                classMatcher = classMatcher.or(matcher.classMatcher());
            }
        }
        AgentEntry.getBasicAgentBuilder().type(classMatcher).transform(new ClassTransformer(matchers))
                .installOn(instrumentation);
    }
}
