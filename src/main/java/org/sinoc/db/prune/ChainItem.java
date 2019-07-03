package org.sinoc.db.prune;

import java.util.Arrays;

import org.sinoc.core.Block;
import org.sinoc.util.FastByteComparisons;

/**
 * Represents a block in the {@link Chain}
 *
 */
class ChainItem {
    long number;
    byte[] hash;
    byte[] parentHash;

    ChainItem(Block block) {
        this.number = block.getNumber();
        this.hash = block.getHash();
        this.parentHash = block.getParentHash();
    }

    ChainItem(long number, byte[] hash, byte[] parentHash) {
        this.number = number;
        this.hash = hash;
        this.parentHash = parentHash;
    }

    boolean isParentOf(ChainItem that) {
        return FastByteComparisons.equal(hash, that.parentHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChainItem that = (ChainItem) o;
        return FastByteComparisons.equal(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return hash != null ? Arrays.hashCode(hash) : 0;
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }
}
