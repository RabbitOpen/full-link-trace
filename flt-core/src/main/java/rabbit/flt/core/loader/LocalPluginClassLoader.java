package rabbit.flt.core.loader;

import rabbit.flt.core.PluginClassLoader;

public class LocalPluginClassLoader extends PluginClassLoader {

    @Override
    public Class<?> loadClassByName(String name) throws Exception {
        return getClass().getClassLoader().loadClass(name);
    }

    @Override
    public String getAgentJarFilePath() {
        throw new UnsupportedOperationException();
    }
}
