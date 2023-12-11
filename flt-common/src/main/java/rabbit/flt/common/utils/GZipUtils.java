package rabbit.flt.common.utils;

import rabbit.flt.common.exception.GZipException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtils {

    private GZipUtils() {
    }

    /**
     * 压缩
     *
     * @param data
     * @return
     */
    public static byte[] zip(byte[] data) {
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                GZIPOutputStream gzipOs = new GZIPOutputStream(out);
        ) {
            gzipOs.write(data);
            gzipOs.flush();
            gzipOs.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new GZipException(e);
        }
    }

    /**
     * 解压缩
     *
     * @param data
     * @param originalSize
     * @return
     */
    public static byte[] unzip(byte[] data, int originalSize) {
        try (
                ByteArrayInputStream is = new ByteArrayInputStream(data);
                GZIPInputStream gzipIs = new GZIPInputStream(is);
        ) {
            byte[] result = new byte[originalSize];
            int length = 0;
            while (true) {
                int read = gzipIs.read(result, length, originalSize - length);
                length += read;
                if (read <= 0) {
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            throw new GZipException(e);
        }
    }

    /**
     * 解压缩
     *
     * @param data
     * @param stepSize
     * @return
     */
    public static byte[] unzipIgnoreOriginalLength(byte[] data, int stepSize) {

        try (
                ByteArrayInputStream is = new ByteArrayInputStream(data);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                GZIPInputStream gzipIs = new GZIPInputStream(is);
        ) {

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
        }
    }
}
