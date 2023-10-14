package rabbit.flt.rpc.common;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static rabbit.flt.rpc.common.ChannelStatus.AUTHENTICATED;
import static rabbit.flt.rpc.common.ChannelStatus.CLOSED;
import static rabbit.flt.rpc.common.ChannelStatus.CONNECTED;
import static rabbit.flt.rpc.common.ChannelStatus.CONNECTING;
import static rabbit.flt.rpc.common.ChannelStatus.INIT;

@RunWith(JUnit4.class)
public class ChannelStatusTest {

    @Test
    public void statusTest() {
        TestCase.assertTrue(INIT.isInit());
        TestCase.assertFalse(INIT.isConnected());
        TestCase.assertFalse(INIT.isAuthenticated());

        TestCase.assertTrue(CONNECTED.isConnected());
        TestCase.assertFalse(CONNECTED.isAuthenticated());

        TestCase.assertFalse(CONNECTING.isConnected());
        TestCase.assertFalse(CONNECTING.isAuthenticated());

        TestCase.assertTrue(AUTHENTICATED.isConnected());
        TestCase.assertTrue(AUTHENTICATED.isAuthenticated());

        TestCase.assertFalse(CLOSED.isConnected());
        TestCase.assertFalse(CLOSED.isAuthenticated());

        TestCase.assertEquals("AUTHENTICATED", AUTHENTICATED.getName());
    }

}
