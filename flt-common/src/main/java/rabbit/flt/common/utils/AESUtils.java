package rabbit.flt.common.utils;

import rabbit.flt.common.exception.AgentException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AESUtils {

    private static final AESUtils inst = new AESUtils();

    private AESUtils() {
    }

    private String algorithm = "AES/ECB/PKCS5Padding";

    /**
     * 加密
     *
     * @param content  内容
     * @param password 密码
     * @return
     */
    public static String encrypt(String content, String password) {
        try {
            SecretKeySpec spec = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(inst.algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, spec);
            return StringUtils.base64Encode(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    /**
     * 解密
     *
     * @param encryptData
     * @param password
     * @return
     */
    public static String decrypt(String encryptData, String password) {
        try {
            byte[] encryptBytes = StringUtils.base64Decode(encryptData);
            SecretKeySpec spec = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(inst.algorithm);
            cipher.init(Cipher.DECRYPT_MODE, spec);
            byte[] decryptBytes = cipher.doFinal(encryptBytes);
            return new String(decryptBytes);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }
}
