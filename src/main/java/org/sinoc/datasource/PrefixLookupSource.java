package org.sinoc.datasource;

/**
 * A kind of source which executes {@link #get(byte[])} query as
 * a {@link DbSource#prefixLookup(byte[], int)} query of backing source.<br>
 *
 * Other operations are simply propagated to backing {@link DbSource}.
 *
 */
public class PrefixLookupSource<V> implements Source<byte[], V> {

    // prefix length in bytes
    private int prefixBytes;
    private DbSource<V> source;

    public PrefixLookupSource(DbSource<V> source, int prefixBytes) {
        this.source = source;
        this.prefixBytes = prefixBytes;
    }

    @Override
    public V get(byte[] key) {
        return source.prefixLookup(key, prefixBytes);
    }

    @Override
    public void put(byte[] key, V val) {
        source.put(key, val);
    }

    @Override
    public void delete(byte[] key) {
        source.delete(key);
    }

    @Override
    public boolean flush() {
        return source.flush();
    }
}
