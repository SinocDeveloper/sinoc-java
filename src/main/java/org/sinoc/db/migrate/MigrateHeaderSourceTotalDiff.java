package org.sinoc.db.migrate;

import java.math.BigInteger;
import org.sinoc.config.SystemProperties;
import org.sinoc.core.Block;
import org.sinoc.core.BlockHeader;
import org.sinoc.core.Blockchain;
import org.sinoc.core.BlockchainImpl;
import org.sinoc.datasource.DataSourceArray;
import org.sinoc.datasource.DbSource;
import org.sinoc.datasource.ObjectDataSource;
import org.sinoc.datasource.Serializers;
import org.sinoc.db.BlockStore;
import org.sinoc.db.DbFlushManager;
import org.sinoc.db.HeaderStore;
import org.sinoc.sync.FastSyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * @deprecated
 * TODO: Remove after a few versions (current: 1.7.3) or with DB version update
 * TODO: Make {@link FastSyncManager#removeHeadersDb(Logger)} private after removing
 * Also remove CommonConfig.headerSource with it as no more used
 *
 * - Repairs Headers DB after FastSync with skipHistory to be usable
 *    a) Updates incorrect total difficulty
 *    b) Migrates headers without index to usable scheme with index
 * - Removes headers DB otherwise as it's not needed
 */
@Deprecated
public class MigrateHeaderSourceTotalDiff implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger("general");

    private ApplicationContext ctx;

    private BlockStore blockStore;

    private Blockchain blockchain;

    private SystemProperties systemProperties;

    public MigrateHeaderSourceTotalDiff(ApplicationContext ctx, BlockStore blockStore,
                                        Blockchain blockchain, SystemProperties systemProperties) {
        this.ctx = ctx;
        this.blockStore = blockStore;
        this.blockchain = blockchain;
        this.systemProperties = systemProperties;
    }

    @Override
    public void run() {
        // checking whether we should do any kind of migration:
        if (!systemProperties.isFastSyncEnabled()) {
            return;
        }

        FastSyncManager fastSyncManager = ctx.getBean(FastSyncManager.class);
        if (fastSyncManager.isInProgress()|| blockStore.getBestBlock().getNumber() == 0) { // Fast sync is not over
            return;
        }

        logger.info("Fast Sync was used. Checking if migration required.");
        boolean dbRemoved = fastSyncManager.removeHeadersDb(logger);
        if (dbRemoved) {
            logger.info("Migration finished.");
            return;
        }
        if (blockStore.getBestBlock().getNumber() > 0 && blockStore.getChainBlockByNumber(1) == null) {
            // Maybe migration of headerStore and totalDifficulty is required?
            HeaderStore headerStore = ctx.getBean(HeaderStore.class);
            if (headerStore.getHeaderByNumber(1) != null) {
                logger.info("No migration required.");
                return;
            }

            logger.info("Migration required. Updating total difficulty.");
            logger.info("=== Don't stop or exit from application, migration could not be resumed ===");
            long firstFullBlockNum = blockStore.getMaxNumber();
            while (blockStore.getChainBlockByNumber(firstFullBlockNum - 1) != null) {
                --firstFullBlockNum;
            }
            Block firstFullBlock = blockStore.getChainBlockByNumber(firstFullBlockNum);
            DbSource<byte[]> headerDbSource = (DbSource<byte[]>) ctx.getBean("headerSource");
            ObjectDataSource<BlockHeader> objectDataSource = new ObjectDataSource<>(headerDbSource, Serializers.BlockHeaderSerializer, 0);
            DataSourceArray<BlockHeader> headerSource = new DataSourceArray<>(objectDataSource);
            BigInteger totalDifficulty = blockStore.getChainBlockByNumber(0).getDifficultyBI();
            for (int i = 1; i < firstFullBlockNum; ++i) {
                totalDifficulty = totalDifficulty.add(headerSource.get(i).getDifficultyBI());
            }
            blockStore.saveBlock(firstFullBlock, totalDifficulty.add(firstFullBlock.getDifficultyBI()), true);
            ((BlockchainImpl) blockchain).updateBlockTotDifficulties(firstFullBlockNum + 1);
            logger.info("Total difficulty updated");
            logger.info("Migrating headerStore");
            int maxHeaderNumber = headerSource.size() - 1;
            DbFlushManager flushManager = ctx.getBean(DbFlushManager.class);
            for (int i = 1; i < headerSource.size(); ++i) {
                BlockHeader curHeader = headerSource.get(i);
                headerStore.saveHeader(curHeader);
                headerSource.set(i, null);
                if (i % 10000 == 0) {
                    logger.info("#{} of {} headers moved. Flushing...", i, maxHeaderNumber);
                    flushManager.commit();
                    flushManager.flush();
                }
            }
            flushManager.commit();
            flushManager.flush();
            logger.info("headerStore migration finished. No more migrations required");
        } else {
            logger.info("No migration required.");
        }
    }
}
