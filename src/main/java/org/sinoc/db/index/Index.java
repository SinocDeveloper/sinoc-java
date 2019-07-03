package org.sinoc.db.index;

import java.util.Collection;

public interface Index extends Iterable<Long> {

    void addAll(Collection<Long> nums);

    void add(Long num);

    Long peek();

    Long poll();

    boolean contains(Long num);

    boolean isEmpty();

    int size();

    void clear();

    void removeAll(Collection<Long> indexes);

    Long peekLast();

    void remove(Long num);
}
