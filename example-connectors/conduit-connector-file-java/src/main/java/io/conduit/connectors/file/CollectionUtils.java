package io.conduit.connectors.file;

import java.util.Collection;

public class CollectionUtils {
    public static <T> int size(Collection<T> collection) {
        return isEmpty(collection) ? 0 : collection.size();
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }
}
