package org.sinoc.sync;

import org.sinoc.config.SystemProperties;
import org.sinoc.core.BlockHeader;
import org.sinoc.core.BlockHeaderWrapper;
import org.sinoc.core.BlockWrapper;
import org.sinoc.db.IndexedBlockStore;
import org.sinoc.validator.BlockHeaderValidator;
import org.sinoc.validator.PocRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

@Component
@Scope("prototype")
public class FastSyncDownloader extends BlockDownloader {
    private final static Logger logger = LoggerFactory.getLogger("sync");

    @Autowired
    SyncPool syncPool;

    @Autowired
    IndexedBlockStore blockStore;

    private SyncQueueReverseImpl syncQueueReverse;

    private PocRule pocRule;

    int counter;
    int maxCount;
    long t;

    @Autowired
    public FastSyncDownloader(BlockHeaderValidator headerValidator, SystemProperties systemProperties) {
        super(headerValidator);
        pocRule = new PocRule(systemProperties.syncForkTrust());
    }

    public void startImporting(BlockHeader start, int count) {
        this.maxCount = count <= 0 ? Integer.MAX_VALUE : count;
        setHeaderQueueLimit(maxCount);
        setBlockQueueLimit(maxCount);

        syncQueueReverse = new SyncQueueReverseImpl(start.getHash(), start.getNumber() - count);
        init(syncQueueReverse, syncPool, "FastSync");
    }

    @Override
    protected void pushBlocks(List<BlockWrapper> blockWrappers) {
        if (!blockWrappers.isEmpty()) {

            for (BlockWrapper blockWrapper : blockWrappers) {
                blockStore.saveBlock(blockWrapper.getBlock(), BigInteger.ZERO, true);
                counter++;
                if (counter >= maxCount) {
                    logger.info("FastSync: All requested " + counter + " blocks are downloaded. (last " +
                            blockWrapper.getBlock().getShortDescr() + ")");
                    stop();
                    break;
                }
            }

            long c = System.currentTimeMillis();
            if (c - t > 5000) {
                t = c;
                logger.info("FastSync: downloaded " + counter + " blocks so far. Last: " +
                        blockWrappers.get(blockWrappers.size() - 1).getBlock().getShortDescr());
                blockStore.flush();
            }
        }
    }

    @Override
    protected void pushHeaders(List<BlockHeaderWrapper> headers) {}

    @Override
    protected int getBlockQueueFreeSize() {
        return Math.max(maxCount - counter, MAX_IN_REQUEST);
    }

    @Override
    protected int getMaxHeadersInQueue() {
        return Math.max(maxCount - syncQueueReverse.getValidatedHeadersCount(), 0);
    }

    // TODO: receipts loading here

    public int getDownloadedBlocksCount() {
        return counter;
    }

    @Override
    protected void finishDownload() {
        blockStore.flush();
    }

    @Override
    protected boolean isValid(BlockHeader header) {
        return super.isValid(header) && pocRule.validateAndLog(header, logger);
    }
}
