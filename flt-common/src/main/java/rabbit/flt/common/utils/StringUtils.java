package rabbit.flt.common.utils;

import java.util.Base64;

public class StringUtils {

    private StringUtils() {
    }

    public static boolean isEmpty(Object obj) {
        return null == obj || "".equals(obj.toString().trim());
    }

    public static String toString(Object obj) {
        return null == obj ? null : obj.toString();
    }

    /**
     * base64编码
     *
     * @param bytes
     * @return
     */
    public static String base64Encode(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }

    /**
     * base64解码
     *
     * @param data
     * @return
     */
    public static byte[] base64Decode(String data) {
        return Base64.getDecoder().decode(data);
    }
}
