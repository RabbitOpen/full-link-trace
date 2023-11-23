package rabbit.flt.core;

import rabbit.flt.common.exception.AgentException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

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
    protected String getAgentJarFilePath() {
        String classResourcePath = AgentEntry.class.getName().replaceAll("\\.", "/").concat(".class");
        String url = ClassLoader.getSystemClassLoader().getResource(classResourcePath).toString();
        url = url.substring(url.indexOf("file:"), url.indexOf('!'));
        try {
            return new File(new URL(url).toURI()).getPath();
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

}
