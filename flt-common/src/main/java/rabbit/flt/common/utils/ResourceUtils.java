package rabbit.flt.common.utils;

import java.io.Closeable;

public class ResourceUtils {

    public static void close(Closeable resource) {
        try {
            if (null == resource) {
                return;
            }
            resource.close();
        } catch (Exception e) {
            // ignore
        }
    }
}
