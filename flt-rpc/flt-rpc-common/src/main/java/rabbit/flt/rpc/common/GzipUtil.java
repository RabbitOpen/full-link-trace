package rabbit.flt.rpc.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {

    /**
     * 压缩
     *
     * @param data
     * @return
     */
    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzipOs = new GZIPOutputStream(out);
            gzipOs.write(data);
            gzipOs.flush();
            gzipOs.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RpcException(e);
        } finally {
            Serializer.close(out);
        }
    }

    /**
     * 解压缩
     * @param data
     * @param originalSize
     * @return
     */
    public static byte[] decompress(byte[] data, int originalSize) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        try {
            byte[] result = new byte[originalSize];
            GZIPInputStream gzipIs = new GZIPInputStream(is);
            int length = 0;
            while (true) {
                int read = gzipIs.read(result, length, originalSize - length);
                length += read;
                if (read <= 0) {
                    break;
                }
            }
            gzipIs.close();
            return result;
        } catch (Exception e) {
            throw new RpcException(e);
        } finally {
            Serializer.close(is);
        }
    }
}
