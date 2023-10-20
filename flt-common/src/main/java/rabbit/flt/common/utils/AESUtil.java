package rabbit.flt.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AESUtil {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * 加密
     *
     * @param content  内容
     * @param password 密码
     * @return
     * @throws Exception
     */
    public static String encrypt(String content, String password) throws Exception {
        SecretKeySpec spec = new SecretKeySpec(password.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        return StringUtils.base64Encode(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 解密
     * @param encryptData
     * @param password
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptData, String password) throws Exception {
        byte[] encryptBytes = StringUtils.base64Decode(encryptData);
        SecretKeySpec spec = new SecretKeySpec(password.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, spec);
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }
}
