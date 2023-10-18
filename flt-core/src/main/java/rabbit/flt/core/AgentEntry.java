package rabbit.flt.core;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import rabbit.flt.common.AbstractConfigFactory;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.utils.ResourceUtil;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.core.factory.DefaultConfigFactory;
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
        AbstractConfigFactory.setFactoryLoader(() -> new DefaultConfigFactory());

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
        AbstractConfigFactory.getFactory().doInitialize(stream);
        ResourceUtil.close(stream);
    }
}
