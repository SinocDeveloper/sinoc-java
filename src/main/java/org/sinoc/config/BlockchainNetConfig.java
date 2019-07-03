package org.sinoc.config;

import org.sinoc.core.BlockHeader;

/**
 * Describes a set of configs for a specific blockchain depending on the block number
 * E.g. the main Ethereum net has at least FrontierConfig and HomesteadConfig depending on the block
 *
 */
public interface BlockchainNetConfig {

    /**
     * Get the config for the specific block
     */
    BlockchainConfig getConfigForBlock(long blockNumber);

    /**
     * Returns the constants common for all the blocks in this blockchain
     */
    Constants getCommonConstants();
}
