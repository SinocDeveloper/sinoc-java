package org.sinoc.db;

import org.sinoc.core.Block;

public abstract class AbstractBlockstore implements BlockStore {

    @Override
    public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
        Block branchBlock = getBlockByHash(branchBlockHash);
        if (branchBlock.getNumber() < blockNumber) {
            throw new IllegalArgumentException("Requested block number > branch hash number: " + blockNumber + " < " + branchBlock.getNumber());
        }
        while(branchBlock.getNumber() > blockNumber) {
            branchBlock = getBlockByHash(branchBlock.getParentHash());
        }
        return branchBlock.getHash();
    }
}
