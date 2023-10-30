package rabbit.flt.common.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.exception.ReflectException;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.utils.AESUtils;
import rabbit.flt.common.utils.ReflectUtils;
import rabbit.flt.common.utils.UUIDUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RunWith(JUnit4.class)
public class UtilTest {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    @Test
    public void uuid() {
        UUID uuid = UUIDUtils.uuid();
        TestCase.assertEquals(1, uuid.version());
        logger.info("uuid time: {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:sss").format(new Date(UUIDUtils.timestamp(uuid.toString()))));
    }

    @Test
    public void aesTest() {
        String password = "1234567812345678";
        String data = Long.toString(System.currentTimeMillis());
        String encrypt = AESUtils.encrypt(data, password);
        TestCase.assertEquals(AESUtils.decrypt(encrypt, password), data);
    }

    @Test
    public void reflectUtilTest() throws NoSuchFieldException {
        ReflectUtils.loadClass(UtilTest.class.getName());
        try {
            ReflectUtils.loadClass(UtilTest.class.getName().concat("abc"));
            throw new RuntimeException();
        } catch (Exception e) {
            TestCase.assertTrue(e instanceof ReflectException);
        }
        User user = ReflectUtils.newInstance(User.class);
        String name = "zhang3";
        Field field = User.class.getDeclaredField("name");
        ReflectUtils.setValue(user, field, name);
        TestCase.assertEquals(name, ReflectUtils.getValue(user, field));
    }

}
