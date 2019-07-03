package org.sinoc.config;

import org.sinoc.datasource.*;
import org.sinoc.db.BlockStore;
import org.sinoc.db.IndexedBlockStore;
import org.sinoc.db.PruneManager;
import org.sinoc.db.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonConfig.class)
public class DefaultConfig {
    private static Logger logger = LoggerFactory.getLogger("general");

    @Autowired
    ApplicationContext appCtx;

    @Autowired
    CommonConfig commonConfig;

    @Autowired
    SystemProperties config;

    public DefaultConfig() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
    }

    @Bean
    public BlockStore blockStore(){
        commonConfig.fastSyncCleanUp();
        IndexedBlockStore indexedBlockStore = new IndexedBlockStore();
        Source<byte[], byte[]> block = commonConfig.cachedDbSource("block");
        Source<byte[], byte[]> index = commonConfig.cachedDbSource("index");
        indexedBlockStore.init(index, block);

        return indexedBlockStore;
    }

    @Bean
    public TransactionStore transactionStore() {
        commonConfig.fastSyncCleanUp();
        return new TransactionStore(commonConfig.cachedDbSource("transactions"));
    }

    @Bean
    public PruneManager pruneManager() {
        if (config.databasePruneDepth() >= 0) {
            return new PruneManager((IndexedBlockStore) blockStore(), commonConfig.stateSource().getJournalSource(),
                    commonConfig.stateSource().getNoJournalSource(), config.databasePruneDepth());
        } else {
            return new PruneManager(null, null, null, -1); // dummy
        }
    }
}
