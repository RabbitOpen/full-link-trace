package rabbit.flt.plugins.springmvc.test;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rabbit.flt.plugins.springmvc.plugin.ControllerPlugin;

import java.lang.reflect.Method;

@RunWith(JUnit4.class)
public class ControllerPluginTest {

    @Test
    public void getPathTest() throws NoSuchMethodException {
        TestCase.assertEquals("/base/valueGet", getPath("valueGet", Controller1.class));
        TestCase.assertEquals("/base/nameGet", getPath("nameGet", Controller1.class));
        TestCase.assertEquals("/base/pathGet", getPath("pathGet", Controller1.class));

        TestCase.assertEquals("/valuePost", getPath("valuePost", Controller2.class));
        TestCase.assertEquals("/namePost", getPath("namePost", Controller2.class));
        TestCase.assertEquals("/pathPost", getPath("pathPost", Controller2.class));

        TestCase.assertEquals("/feign/valuePost", getPath("valuePost", Controller3.class));
        TestCase.assertEquals("/feign/namePost", getPath("namePost", Controller3.class));
        TestCase.assertEquals("/feign/pathPost", getPath("pathPost", Controller3.class));
    }

    private String getPath(String name, Class<?> controllerClz) throws NoSuchMethodException {
        Method method = getMethod(name, controllerClz);
        return new TestControllerPlugin().getMethodPath(method, controllerClz);
    }
    private Method getMethod(String name, Class<?> target) throws NoSuchMethodException {
        return target.getDeclaredMethod(name);
    }

    public class TestControllerPlugin extends ControllerPlugin {

        @Override
        public String getMethodPath(Method method, Class<?> controllerClz) {
            return super.getMethodPath(method, controllerClz);
        }
    }
}
