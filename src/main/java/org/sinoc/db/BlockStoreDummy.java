package org.sinoc.db;

import java.math.BigInteger;

import java.util.List;

import org.sinoc.core.Block;
import org.sinoc.core.BlockHeader;
import org.sinoc.crypto.HashUtil;

public class BlockStoreDummy implements BlockStore {

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {

        byte[] data = String.valueOf(blockNumber).getBytes();
        return HashUtil.sha3(data);
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
        return getBlockHashByNumber(blockNumber);
    }

    @Override
    public Block getChainBlockByNumber(long blockNumber) {
        return null;
    }

    @Override
    public Block getBlockByHash(byte[] hash) {
        return null;
    }

    @Override
    public boolean isBlockExist(byte[] hash) {
        return false;
    }

    @Override
    public List<byte[]> getListHashesEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public List<BlockHeader> getListHeadersEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public List<Block> getListBlocksEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public void saveBlock(Block block, BigInteger totalDifficulty, boolean mainChain) {

    }


    @Override
    public BigInteger getTotalDifficulty() {
        return null;
    }

    @Override
    public Block getBestBlock() {
        return null;
    }


    @Override
    public void flush() {
    }

    @Override
    public void load() {
    }

    @Override
    public long getMaxNumber() {
        return 0;
    }


    @Override
    public void reBranch(Block forkBlock) {

    }

    @Override
    public BigInteger getTotalDifficultyForHash(byte[] hash) {
        return null;
    }

    @Override
    public void close() {}
}
