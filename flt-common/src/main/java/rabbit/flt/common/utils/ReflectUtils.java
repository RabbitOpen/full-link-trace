package rabbit.flt.common.utils;


import rabbit.flt.common.exception.ReflectException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {

    private ReflectUtils() {
    }

    /**
     * 写入值
     * @param bean
     * @param field
     * @param value
     */
    public static void setValue(Object bean, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(bean, value);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 取值
     * @param bean
     * @param field
     * @param <T>
     * @return
     */
    public static <T> T getValue(Object bean, Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(bean);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public static <T> T newInstance(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 加载指定class
     * @param clzName
     * @return
     */
    public static <T> Class<T> loadClass(String clzName) {
        try {
            return (Class<T>) Class.forName(clzName);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 获取方法
     * @param clz
     * @param methodName
     * @param argsType
     * @return
     */
    public static Method loadMethod(Class<?> clz, String methodName, Class<?>... argsType) {
        try {
            return clz.getDeclaredMethod(methodName, argsType);
        } catch (NoSuchMethodException e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 获取字段
     * @param clz
     * @param fieldName
     * @return
     */
    public static Field loadField(Class<?> clz, String fieldName) {
        try {
            return clz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new ReflectException(e);
        }
    }

    /**
     * 是否包含指定class
     *
     * @param clzName
     * @return
     */
    public static boolean hasClass(String clzName) {
        try {
            Class.forName(clzName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
