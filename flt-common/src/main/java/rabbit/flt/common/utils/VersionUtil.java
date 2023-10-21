package rabbit.flt.common.utils;

import rabbit.flt.common.exception.AgentException;

import java.io.InputStream;
import java.util.Properties;

/**
 * 版本工具
 */
public class VersionUtil {

    private static String version;

    private VersionUtil() {}

    public static String getVersion() {
        if (!StringUtils.isEmpty(version)) {
            return version;
        }
        version = new VersionUtil().loadVersion();
        return version;
    }

    private String loadVersion() {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("flt.properties");
        try {
            Properties properties = new Properties();
            properties.load(resource);
            return StringUtils.toString(properties.get("version"));
        } catch (Exception e) {
            throw new AgentException(e);
        } finally {
            ResourceUtil.close(resource);
        }
    }
}
