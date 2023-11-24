package rabbit.flt.common.utils;

import rabbit.flt.common.exception.AgentException;

import java.io.InputStream;
import java.util.Properties;

/**
 * 版本工具
 */
public class VersionUtils {

    private static String version;

    private VersionUtils() {
    }

    public static String getVersion() {
        if (!StringUtils.isEmpty(version)) {
            return version;
        }
        version = getVersion("flt.properties", "version");
        return version;
    }

    public static String getVersion(String resourceFile, String propertyName) {
        return new VersionUtils().loadProperty(resourceFile, propertyName);
    }

    private String loadProperty(String resourceFile, String propertyName) {
        InputStream resource = getClass().getClassLoader().getResourceAsStream(resourceFile);
        try {
            Properties properties = new Properties();
            properties.load(resource);
            return StringUtils.toString(properties.get(propertyName));
        } catch (Exception e) {
            throw new AgentException(e);
        } finally {
            ResourceUtils.close(resource);
        }
    }
}
