package org.sinoc.core;


import org.sinoc.db.ByteArrayWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chain {

    private static final Logger logger = LoggerFactory.getLogger("blockchain");

    private List<Block> chain = new ArrayList<>();
    private BigInteger totalDifficulty = BigInteger.ZERO;
    private Map<ByteArrayWrapper, Block> index = new HashMap<>();


    public boolean tryToConnect(Block block) {

        if (chain.isEmpty()) {
            add(block);
            return true;
        }

        Block lastBlock = chain.get(chain.size() - 1);
        if (lastBlock.isParentOf(block)) {
            add(block);
            return true;
        }
        return false;
    }

    public void add(Block block) {
        logger.info("adding block to alt chain block.hash: [{}] ", block.getShortHash());
        totalDifficulty = totalDifficulty.add(block.getDifficultyBI());
        logger.info("total difficulty on alt chain is: [{}] ", totalDifficulty);
        chain.add(block);
        index.put(new ByteArrayWrapper(block.getHash()), block);
    }

    public Block get(int i) {
        return chain.get(i);
    }

    public Block getLast() {
        return chain.get(chain.size() - 1);
    }

    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public boolean isParentOnTheChain(Block block) {
        return (index.get(new ByteArrayWrapper(block.getParentHash())) != null);
    }

    public long getSize() {
        return chain.size();
    }


}
