package rabbit.flt.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * 插件类加载器
 */
public abstract class PluginClassLoader {

    private static PluginClassLoader pluginClassLoader;

    public static PluginClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    public static void setPluginClassLoader(PluginClassLoader pluginClassLoader) {
        PluginClassLoader.pluginClassLoader = pluginClassLoader;
    }

    /**
     * 加载类
     * @param name
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public abstract Class<?> loadClassByName(String name) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException;

    /**
     * 获取代理类路径
     * @return
     */
    public abstract String getAgentJarFilePath();

}
