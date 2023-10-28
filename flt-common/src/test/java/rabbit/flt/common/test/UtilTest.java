package rabbit.flt.common.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.common.log.AgentLoggerFactory;
import rabbit.flt.common.log.Logger;
import rabbit.flt.common.utils.AESUtils;
import rabbit.flt.common.utils.UUIDUtils;

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
    public void aesTest() throws Exception {
        String password = "1234567812345678";
        String data = Long.toString(System.currentTimeMillis());
        String encrypt = AESUtils.encrypt(data, password);
        TestCase.assertEquals(AESUtils.decrypt(encrypt, password), data);
    }
}
