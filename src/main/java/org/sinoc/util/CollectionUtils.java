package org.sinoc.util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

public class CollectionUtils {
    public static <T> List<T> truncate(final List<T> items, int limit) {
        if(limit > items.size()) {
            return new ArrayList<>(items);
        }
        List<T> truncated = new ArrayList<>(limit);
        for(T item : items) {
            truncated.add(item);
            if(truncated.size() == limit) {
                break;
            }
        }
        return truncated;
    }

    public static <T> List<T> truncateRand(final List<T> items, int limit) {
        if(limit > items.size()) {
            return new ArrayList<>(items);
        }
        List<T> truncated = new ArrayList<>(limit);

        LinkedList<Integer> index = new LinkedList<>();
        for (int i = 0; i < items.size(); ++i) {
            index.add(i);
        }

        if (limit * 2 < items.size()) {
            // Limit is very small comparing to items.size()
            Set<Integer> smallIndex = new HashSet<>();
            for (int i = 0; i < limit; ++i) {
                int randomNum = ThreadLocalRandom.current().nextInt(0, index.size());
                smallIndex.add(index.remove(randomNum));
            }
            smallIndex.forEach(i -> truncated.add(items.get(i)));
        } else {
            // Limit is compared to items.size()
            for (int i = 0; i < items.size() - limit; ++i) {
                int randomNum = ThreadLocalRandom.current().nextInt(0, index.size());
                index.remove(randomNum);
            }
            index.forEach(i -> truncated.add(items.get(i)));
        }

        return truncated;
    }
}
