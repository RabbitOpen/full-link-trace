package rabbit.flt.core.loader;

import rabbit.flt.common.exception.AgentException;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.utils.ResourceUtils;
import rabbit.flt.core.AgentEntry;
import rabbit.flt.core.PluginClassLoader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 默认类加载器，适用于agent方式启动
 */
public class DefaultPluginClassLoader extends PluginClassLoader {

    // agent jar file
    private JarFile jarFile;

    private Method defineClassMethod;

    private final Map<String, Class<?>> clzMap = new ConcurrentHashMap<>();

    private final Logger logger = AgentLoggerFactory.getLogger(getClass());

    public DefaultPluginClassLoader() {
        try {
            defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass",
                    String.class, byte[].class, int.class, int.class);
            defineClassMethod.setAccessible(true);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public Class<?> loadClassByName(String name) throws Exception {
        if (clzMap.containsKey(name)) {
            return clzMap.get(name);
        }
        synchronized (DefaultPluginClassLoader.class) {
            tryLoadBaseClass();
            if (clzMap.containsKey(name)) {
                return clzMap.get(name);
            }
            Class<?> clz = findClass(name);
            clzMap.put(name, clz);
            return clz;
        }
    }

    /**
     * 加载基类
     * @throws Exception
     */
    private void tryLoadBaseClass() throws Exception {
        String performancePlugin = "rabbit.flt.plugins.common.plugin.PerformancePlugin";
        if (!clzMap.containsKey(performancePlugin)) {
            clzMap.put(performancePlugin, findClass(performancePlugin));
        }
        String supportPlugin = "rabbit.flt.plugins.common.plugin.SupportPlugin";
        if (!clzMap.containsKey(supportPlugin)) {
            clzMap.put(supportPlugin, findClass(supportPlugin));
        }
        String constructorPlugin = "rabbit.flt.plugins.common.plugin.ConstructorPlugin";
        if (!clzMap.containsKey(constructorPlugin)) {
            clzMap.put(constructorPlugin, findClass(constructorPlugin));
        }
    }

    protected Class<?> findClass(String name) throws Exception {
        Class<?> clz = loadClassFromJar(name);
        logger.info("load class[{}] with class loader[{}]", name, clz.getClassLoader().getClass().getName());
        return clz;
    }

    /**
     * 从jar file中加载指定类
     * @param name
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Class<?> loadClassFromJar(String name) throws Exception {
        if (null == jarFile) {
            jarFile = new JarFile(getAgentJarFilePath());
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String loadName = loader.getClass().getName();
        if ("sun.misc.Launcher$AppClassLoader".equals(loadName)) {
            return loader.loadClass(name);
        }
        String path = name.replace('.', '/').concat(".class");
        JarEntry entry = jarFile.getJarEntry(path);
        if (null == entry) {
            throw new ClassNotFoundException(name);
        }
        URL classFileUrl = new URL("jar:file:".concat(getAgentJarFilePath()).concat("!/").concat(path));
        byte[] data;
        BufferedInputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            is = new BufferedInputStream(classFileUrl.openStream());
            os = new ByteArrayOutputStream();
            int ch;
            while (-1 != (ch = is.read())) {
                os.write(ch);
            }
            data = os.toByteArray();
        } finally {
            ResourceUtils.close(is);
            ResourceUtils.close(os);
        }
        return (Class<?>) defineClassMethod.invoke(loader, name, data, 0, data.length);
    }

    @Override
    public String getAgentJarFilePath() {
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
