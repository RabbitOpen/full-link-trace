package rabbit.flt.core;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.AgentConfig;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.utils.ResourceUtils;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.core.factory.DefaultConfigFactory;
import rabbit.flt.core.loader.DefaultPluginClassLoader;
import rabbit.flt.core.transformer.ClassTransformer;
import rabbit.flt.plugins.common.Matcher;
import rabbit.flt.plugins.common.MetricsPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static rabbit.flt.common.AbstractConfigFactory.CLASS_PATH_PREFIX;

/**
 * 代理人口
 */
public class AgentEntry {

    private static final Logger logger = AgentLoggerFactory.getLogger(AgentEntry.class);

    public static void premain(String agentConfig, Instrumentation inst) throws Exception {
        if (StringUtils.isEmpty(agentConfig)) {
            return;
        }
        AbstractConfigFactory.setFactoryLoader(DefaultConfigFactory::new);

        // 初始化
        PluginClassLoader.setPluginClassLoader(new DefaultPluginClassLoader());

        // 设置配置文件
        AbstractConfigFactory.setAgentConfigFile(agentConfig);

        tryInitFactory(agentConfig);

        // 加载指标插件类
        loadMetricsPlugin();

        MetricsPlugin.tryStartingAllPlugins();

        List<Matcher> matchers = Matcher.loadMatchers();
        ElementMatcher.Junction<TypeDescription> classMatcher = null;
        for (Matcher matcher : matchers) {
            if (null == classMatcher) {
                classMatcher = matcher.classMatcher();
            } else {
                classMatcher = classMatcher.or(matcher.classMatcher());
            }
        }
        getBasicAgentBuilder().type(classMatcher).transform(new ClassTransformer(matchers))
                .with(getEmptyListener()).installOn(inst);
    }

    private static AgentBuilder.Listener getEmptyListener() {
        return new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                // ignore
            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
                // ignore
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
                // ignore
            }

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
                // ignore
            }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                // ignore
            }
        };
    }

    public static AgentBuilder getBasicAgentBuilder() {
        ElementMatcher.Junction<NamedElement> ignoreMatcher = nameStartsWith("org.slf4j.")
                .or(nameStartsWith("com.intellij."))
                .or(nameStartsWith("java."))
                .or(nameStartsWith("javax."));
        AgentConfig config = AbstractConfigFactory.getConfig();
        if (null != config) {
            String ignorePackages = config.getIgnorePackages();
            if (!StringUtils.isEmpty(ignorePackages)) {
                for (String pkg : ignorePackages.split(",")) {
                    ignoreMatcher = ignoreMatcher.or(nameStartsWith(pkg.trim()));
                    logger.info("ignore package: {}", pkg);
                }
            }
            String ignoreClasses = config.getIgnoreClasses();
            if (!StringUtils.isEmpty(ignoreClasses)) {
                for (String clz : ignoreClasses.split(",")) {
                    ignoreMatcher = ignoreMatcher.or(named(clz.trim()));
                    logger.info("ignore class: {}", clz);
                }
            }
        }
        return new AgentBuilder.Default().ignore(ignoreMatcher);
    }

    /**
     * 加载指标插件
     */
    private static void loadMetricsPlugin() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            String pluginPath = new File(PluginClassLoader.getPluginClassLoader().getAgentJarFilePath()).getParent().concat("/metrics-plugin");
            File dir = new File(pluginPath);
            if (dir.exists() && dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        addURL.invoke(classLoader, file.toURI().toURL());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 尝试初始化配置工厂
     *
     * @param agentConfig
     * @throws FileNotFoundException
     */
    private static void tryInitFactory(String agentConfig) throws FileNotFoundException {
        if (agentConfig.toLowerCase().startsWith(CLASS_PATH_PREFIX)) {
            return;
        }
        File file = new File(agentConfig);
        if (!file.exists() || !file.isFile()) {
            file = new File(new File(PluginClassLoader.getPluginClassLoader().getAgentJarFilePath()).getParent()
                    .concat("/").concat(agentConfig));
        }
        FileInputStream stream = new FileInputStream(file);
        DefaultConfigFactory factory = (DefaultConfigFactory) AbstractConfigFactory.getFactory();
        factory.setConfig(factory.loadConfig(stream));
        ResourceUtils.close(stream);
    }
}
