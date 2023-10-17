package rabbit.flt.common.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.utils.AESUtil;
import rabbit.flt.common.utils.UUIDUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RunWith(JUnit4.class)
public class UtilTest {

    private Logger logger = AgentLoggerFactory.getLogger(getClass());

    @Test
    public void uuid() {
        UUID uuid = UUIDUtil.uuid();
        TestCase.assertEquals(1, uuid.version());
        logger.info("uuid time: {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:sss").format(new Date(UUIDUtil.timestamp(uuid.toString()))));
    }

    @Test
    public void aesTest() throws Exception {
        String password = "1234567812345678";
        String data = Long.toString(System.currentTimeMillis());
        String encrypt = AESUtil.encrypt(data, password);
        TestCase.assertEquals(AESUtil.decrypt(encrypt, password), data);
    }
}
