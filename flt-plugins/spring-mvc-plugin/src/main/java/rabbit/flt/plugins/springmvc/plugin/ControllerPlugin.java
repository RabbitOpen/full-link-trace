package rabbit.flt.plugins.springmvc.plugin;

import rabbit.flt.common.context.TraceContext;
import rabbit.flt.common.trace.TraceData;
import rabbit.flt.common.trace.io.HttpRequest;
import rabbit.flt.common.utils.CollectionUtils;
import rabbit.flt.common.utils.StringUtils;
import rabbit.flt.plugins.common.plugin.PerformancePlugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerPlugin extends PerformancePlugin {

    private List<Class<? extends Annotation>> annotationClasses;

    /**
     * 方法 请求路径 缓存
     */
    private static final Map<Method, String> requestPathCache = new ConcurrentHashMap<>();

    @Override
    public Object[] before(Object target, Method method, Object[] args) {
        if (!isTraceOpened()) {
            return args;
        }
        fillSupportTraceData(method, target);
        return super.before(target, method, args);
    }

    /**
     * 填充support trace
     *
     * @param method
     * @param controller
     */
    private void fillSupportTraceData(Method method, Object controller) {
        TraceData supportTraceData = TraceContext.getWebTraceDataContextData();
        if (null == supportTraceData) {
            return;
        }
        String path = getAndCacheMethodPath(method, controller.getClass());
        supportTraceData.setNodeName(path);
        HttpRequest request = supportTraceData.getHttpRequest();
        if (null != request) {
            // 兼容web flux
            if (!"/".equals(request.getContextPath())) {
                supportTraceData.setNodeName(request.getContextPath().concat(path));
            }
            supportTraceData.setNodeDesc(request.getMethod().concat(": ").concat(supportTraceData.getNodeName()));
        }
        supportTraceData.setHasMappedController(true);
        supportTraceData.setData(method.getDeclaringClass().getName().concat(".").concat(method.getName()));
        TraceContext.removeWebTraceContextData();
    }

    private String getAndCacheMethodPath(Method method, Class<?> controllerClz) {
        return requestPathCache.computeIfAbsent(method, m -> getMethodPath(method, controllerClz));
    }

    protected String getMethodPath(Method method, Class<?> controllerClz) {
        try {
            Annotation annotation = getTargetAnnotation(method);
            if (null == annotation) {
                // 从基类方法找注解
                annotation = getMethodAnnotationFromSupperClz(method, controllerClz);
            }
            String value = getPathFromAnnotation(annotation);
            if (!StringUtils.isEmpty(value)) {
                value = getBaseRequestPath(controllerClz).concat(value);
                logger.info("method: {}, path: {}",  method.getName(), value);
                return value;
            }
            logger.error("method: {}.{}, ---- UNKNOWN PATH ----", method.getDeclaringClass().getName(),
                    method.getName());
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return "UNKNOWN PATH";
    }

    private String getBaseRequestPath(Class<?> controllerClz) throws ClassNotFoundException {
        Class<Annotation> annClz = (Class<Annotation>) loadClass("org.springframework.web.bind.annotation.RequestMapping");
        return getPathFromAnnotation(controllerClz.getAnnotation(annClz));
    }
    /**
     * 读取注解上的path
     * @param annotation
     * @return
     */
    private String getPathFromAnnotation(Annotation annotation) {
        try {
            if (null == annotation) {
                return "";
            }
            Method m = annotation.getClass().getDeclaredMethod("value");
            String[] value = (String[]) m.invoke(annotation);
            if (!CollectionUtils.isEmpty(value)) {
                return value[0];
            }
            m = annotation.getClass().getDeclaredMethod("name");
            String name = StringUtils.toString(m.invoke(annotation));
            if (!StringUtils.isEmpty(name)) {
                return name;
            }
            m = annotation.getClass().getDeclaredMethod("path");
            Object result = m.invoke(annotation);
            if (null != result) {
                if (result.getClass().isArray()) {
                    Object[] paths = (Object[]) result;
                    return CollectionUtils.isEmpty(paths) ? "" : StringUtils.toString(paths[0]);
                } else {
                    String path = StringUtils.toString(result);
                    if (!StringUtils.isEmpty(path)) {
                        return path;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "";
    }

    /**
     * 获取基类方法上的注解
     *
     * @param method
     * @param controllerClz
     * @return
     */
    private Annotation getMethodAnnotationFromSupperClz(Method method, Class<?> controllerClz) {
        List<Type> superInterfaces = new ArrayList<>();
        loadAllSuperInterfaces(superInterfaces, controllerClz.getGenericInterfaces());
        List<Class<? extends Annotation>> targetAnnotations = getAnnotationClasses();
        for (Type type : superInterfaces) {
            for (Class<? extends Annotation> annClz : targetAnnotations) {
                try {
                    Method declaredMethod = ((Class<?>) type).getDeclaredMethod(method.getName(), method.getParameterTypes());
                    Annotation ann = declaredMethod.getAnnotation(annClz);
                    if (null != ann) {
                        return ann;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }

    private void loadAllSuperInterfaces(List<Type> list, Type[] superInterfaces) {
        for (Type type : superInterfaces) {
            list.add(type);
            loadAllSuperInterfaces(list, ((Class<?>) type).getGenericInterfaces());
        }
    }

    /**
     * 找寻方法注解
     *
     * @param method
     * @return
     */
    private Annotation getTargetAnnotation(Method method) {
        for (Class<? extends Annotation> annClz : getAnnotationClasses()) {
            Annotation annotation = method.getAnnotation(annClz);
            if (null != annotation) {
                return annotation;
            }
        }
        return null;
    }

    private List<Class<? extends Annotation>> getAnnotationClasses() {
        if (null != annotationClasses) {
            return annotationClasses;
        }
        String[] names = new String[]{
                "org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.GetMapping",
                "org.springframework.web.bind.annotation.RequestMapping",
                "org.springframework.web.bind.annotation.DeleteMapping",
                "org.springframework.web.bind.annotation.PutMapping",
                "org.springframework.web.bind.annotation.PatchMapping"
        };
        List<Class<? extends Annotation>> list = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            try {
                list.add((Class<? extends Annotation>) loadClass(names[i]));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        annotationClasses = list;
        return annotationClasses;
    }
}
