package rabbit.flt.rpc.common;

import rabbit.flt.common.exception.GZipException;
import rabbit.flt.common.utils.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtils {

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
            throw new GZipException(e);
        } finally {
            ResourceUtils.close(out);
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
            throw new GZipException(e);
        } finally {
            ResourceUtils.close(is);
        }
    }

    /**
     * 解压缩
     * @param data
     * @param stepSize
     * @return
     */
    public static byte[] decompressIgnoreOriginalLength(byte[] data, int stepSize) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPInputStream gzipIs = null;
        try {
            gzipIs = new GZIPInputStream(is);
            byte[] bytes = new byte[stepSize];
            while (true) {
                int read = gzipIs.read(bytes, 0, bytes.length);
                if (read > 0) {
                    os.write(bytes, 0, read);
                } else {
                    break;
                }
            }
            return os.toByteArray();
        } catch (Exception e) {
            throw new GZipException(e);
        } finally {
            ResourceUtils.close(gzipIs);
            ResourceUtils.close(os);
            ResourceUtils.close(is);
        }
    }
}
