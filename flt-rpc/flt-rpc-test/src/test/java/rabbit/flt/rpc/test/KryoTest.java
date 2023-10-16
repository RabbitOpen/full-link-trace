package rabbit.flt.rpc.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rabbit.flt.rpc.common.Serializer;

@RunWith(JUnit4.class)
public class KryoTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void kryoTest() {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 102400; i++) {
            data.append("123456789abcdef123456789abcdef123456789abcdef123456789abcdef");
        }
        byte[] serialize = Serializer.serialize(data.toString());
        logger.info("size: {}", serialize.length);
        TestCase.assertEquals(data.toString(), Serializer.deserialize(serialize));
    }
}
