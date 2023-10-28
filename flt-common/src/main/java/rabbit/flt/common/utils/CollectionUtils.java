package rabbit.flt.common.utils;

import java.util.Collection;

public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return null == collection || collection.isEmpty();
    }

    public static <T> boolean isEmpty(T[] arr) {
        return null == arr || 0 == arr.length;
    }
}
