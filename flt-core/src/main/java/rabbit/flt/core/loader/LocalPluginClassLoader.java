package rabbit.flt.core.loader;

import rabbit.flt.core.PluginClassLoader;

public class LocalPluginClassLoader extends PluginClassLoader {

    @Override
    public Class<?> loadClassByName(String name) throws ClassNotFoundException {
        return getClass().getClassLoader().loadClass(name);
    }

}
