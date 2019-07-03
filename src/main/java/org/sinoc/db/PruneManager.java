package org.sinoc.db;

import org.sinoc.config.SystemProperties;
import org.sinoc.core.Block;
import org.sinoc.core.BlockHeader;
import org.sinoc.datasource.JournalSource;
import org.sinoc.datasource.Source;
import org.sinoc.db.prune.Pruner;
import org.sinoc.db.prune.Segment;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages state pruning part of block processing.
 *
 * <p>
 *     Constructs chain segments and prune them when they are complete
 *
 * Created by Anton Nashatyrev on 10.11.2016.
 *
 * @see Segment
 * @see Pruner
 */
public class PruneManager {

    private static final int LONGEST_CHAIN = 192;

    private JournalSource<?> journalSource;

    @Autowired
    private IndexedBlockStore blockStore;

    private int pruneBlocksCnt;

    private Segment segment;
    private Pruner pruner;

    @Autowired
    private PruneManager(SystemProperties config) {
        pruneBlocksCnt = config.databasePruneDepth();
    }

    public PruneManager(IndexedBlockStore blockStore, JournalSource<?> journalSource,
                        Source<byte[], ?> pruneStorage, int pruneBlocksCnt) {
        this.blockStore = blockStore;
        this.journalSource = journalSource;
        this.pruneBlocksCnt = pruneBlocksCnt;

        if (journalSource != null && pruneStorage != null)
            this.pruner = new Pruner(journalSource.getJournal(), pruneStorage);
    }

    @Autowired
    public void setStateSource(StateSource stateSource) {
        journalSource = stateSource.getJournalSource();
        if (journalSource != null)
            pruner = new Pruner(journalSource.getJournal(), stateSource.getNoJournalSource());
    }

    public void blockCommitted(BlockHeader block) {
        if (pruneBlocksCnt < 0) return; // pruning disabled

        JournalSource.Update update = journalSource.commitUpdates(block.getHash());
        pruner.feed(update);

        long forkBlockNum = block.getNumber() - getForkBlocksCnt();
        if (forkBlockNum < 0) return;

        List<Block> pruneBlocks = blockStore.getBlocksByNumber(forkBlockNum);
        Block chainBlock = blockStore.getChainBlockByNumber(forkBlockNum);

        // reset segment and return
        // if chainBlock is accidentally null
        if (chainBlock == null) {
            segment = null;
            return;
        }

        if (segment == null) {
            if (pruneBlocks.size() == 1)    // wait for a single chain
                segment = new Segment(chainBlock);
            return;
        }

        Segment.Tracker tracker = segment.startTracking();
        tracker.addMain(chainBlock);
        tracker.addAll(pruneBlocks);
        tracker.commit();

        if (segment.isComplete()) {
            if (!pruner.isReady()) {
                List<byte[]> forkWindow = getAllChainsHashes(segment.getRootNumber() + 1, blockStore.getMaxNumber());
                pruner.init(forkWindow, getForkBlocksCnt());

                int mainChainWindowSize = pruneBlocksCnt - getForkBlocksCnt();
                if (mainChainWindowSize > 0) {
                    List<byte[]> mainChainWindow = getMainChainHashes(Math.max(1, segment.getRootNumber() - mainChainWindowSize + 1),
                            segment.getRootNumber());
                    pruner.withSecondStep(mainChainWindow, mainChainWindowSize);
                }
            }
            pruner.prune(segment);
            segment = new Segment(chainBlock);
        }

        long mainBlockNum = block.getNumber() - getMainBlocksCnt();
        if (mainBlockNum < 0) return;

        byte[] hash = blockStore.getBlockHashByNumber(mainBlockNum);
        pruner.persist(hash);
    }

    private int getForkBlocksCnt() {
        return Math.min(pruneBlocksCnt, 2 * LONGEST_CHAIN);
    }

    private int getMainBlocksCnt() {
        if (pruneBlocksCnt <= 2 * LONGEST_CHAIN) {
            return Integer.MAX_VALUE;
        } else {
            return pruneBlocksCnt;
        }
    }

    private List<byte[]> getAllChainsHashes(long fromBlock, long toBlock) {
        List<byte[]> ret = new ArrayList<>();
        for (long num = fromBlock; num <= toBlock; num++) {
            List<Block> blocks = blockStore.getBlocksByNumber(num);
            List<byte[]> hashes = blocks.stream().map(Block::getHash).collect(Collectors.toList());
            ret.addAll(hashes);
        }
        return ret;
    }

    private List<byte[]> getMainChainHashes(long fromBlock, long toBlock) {
        List<byte[]> ret = new ArrayList<>();
        for (long num = fromBlock; num <= toBlock; num++) {
            byte[] hash = blockStore.getBlockHashByNumber(num);
            ret.add(hash);
        }
        return ret;
    }
}
