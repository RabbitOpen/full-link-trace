package rabbit.flt.common.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;

public class StringUtils {

    private StringUtils() {}

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
        return new BASE64Encoder().encode(bytes);
    }

    /**
     * base64解码
     *
     * @param data
     * @return
     */
    public static byte[] base64Decode(String data) throws IOException {
        return new BASE64Decoder().decodeBuffer(data);
    }
}
