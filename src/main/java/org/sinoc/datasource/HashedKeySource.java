package org.sinoc.datasource;

/**
 * Indicator interface which narrows the Source contract:
 * the same Key always maps to the same Value,
 * there could be no put() with the same Key and different Value
 * Normally the Key is the hash of the Value
 * Usually such kind of sources are Merkle Trie backing stores
 *
 */
public interface HashedKeySource<Key, Value> extends Source<Key, Value> {
}
