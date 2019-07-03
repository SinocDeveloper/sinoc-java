package org.sinoc.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MinMaxMap<V> extends TreeMap<Long, V> {

    public void clearAllAfter(long key) {
        if (isEmpty()) return;
        navigableKeySet().subSet(key, false, getMax(), true).clear();
    }

    public void clearAllBefore(long key) {
        if (isEmpty()) return;
        descendingKeySet().subSet(key, false, getMin(), true).clear();
    }

    public Long getMin() {
        return isEmpty() ? null : firstKey();
    }

    public Long getMax() {
        return isEmpty() ? null : lastKey();
    }
}
