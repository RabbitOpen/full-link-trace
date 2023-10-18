package rabbit.flt.common.utils;

import java.util.Collection;

public class CollectionUtil {

    public static boolean isEmpty(Collection<?> collection) {
        return null == collection || 0 == collection.size();
    }

    public static <T> boolean isEmpty(T[] arr) {
        return null == arr || 0 == arr.length;
    }
}
