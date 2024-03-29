package org.sinoc.datasource.inmem;

import java.util.Map;
import java.util.Set;

import org.sinoc.datasource.DbSettings;
import org.sinoc.datasource.DbSource;
import org.sinoc.util.ByteArrayMap;
import org.sinoc.util.FastByteComparisons;

public class HashMapDBSimple<V> implements DbSource<V> {

    protected final Map<byte[], V> storage;

    public HashMapDBSimple() {
        this(new ByteArrayMap<V>());
    }

    public HashMapDBSimple(ByteArrayMap<V> storage) {
        this.storage = storage;
    }

    @Override
    public void put(byte[] key, V val) {
        if (val == null) {
            delete(key);
        } else {
            storage.put(key, val);
        }
    }

    @Override
    public V get(byte[] key) {
        return storage.get(key);
    }

    @Override
    public void delete(byte[] key) {
        storage.remove(key);
    }

    @Override
    public boolean flush() {
        return true;
    }

    @Override
    public void setName(String name) {}

    @Override
    public String getName() {
        return "in-memory";
    }

    @Override
    public void init() {}

    @Override
    public void init(DbSettings settings) {}

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void close() {}

    @Override
    public Set<byte[]> keys() {
        return getStorage().keySet();
    }

    @Override
    public void reset() {
        storage.clear();
    }

    @Override
    public V prefixLookup(byte[] key, int prefixBytes) {

        for (Map.Entry<byte[], V> e : storage.entrySet())
            if (FastByteComparisons.compareTo(key, 0, prefixBytes, e.getKey(), 0, prefixBytes) == 0) {
                return e.getValue();
            }

        return null;
    }

    @Override
    public void updateBatch(Map<byte[], V> rows) {
        for (Map.Entry<byte[], V> entry : rows.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public Map<byte[], V> getStorage() {
        return storage;
    }
}
