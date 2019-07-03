package org.sinoc.shell.model.dto;

import org.sinoc.shell.model.dto.NetworkInfoDTO.SyncStatusDTO;

public class BlockchainInfoDTO {

	private final Long highestBlockNumber;

	private final Long lastBlockNumber;

	/**
	 * UTC time in seconds
	 */
	private final Long lastBlockTime;

	private final Integer lastBlockTransactions;

	private final Long difficulty;

	// Not used now
	private final Long lastReforkTime;

	private final Long networkHashRate;

	private final Long gasPrice;

	private final NetworkInfoDTO.SyncStatusDTO syncStatus;

	public BlockchainInfoDTO(Long highestBlockNumber, Long lastBlockNumber, Long lastBlockTime,
			Integer lastBlockTransactions, Long difficulty, Long lastReforkTime, Long networkHashRate, Long gasPrice,
			SyncStatusDTO syncStatus) {
		this.highestBlockNumber = highestBlockNumber;
		this.lastBlockNumber = lastBlockNumber;
		this.lastBlockTime = lastBlockTime;
		this.lastBlockTransactions = lastBlockTransactions;
		this.difficulty = difficulty;
		this.lastReforkTime = lastReforkTime;
		this.networkHashRate = networkHashRate;
		this.gasPrice = gasPrice;
		this.syncStatus = syncStatus;
	}

	public Long getHighestBlockNumber() {
		return highestBlockNumber;
	}

	public Long getLastBlockNumber() {
		return lastBlockNumber;
	}

	public Long getLastBlockTime() {
		return lastBlockTime;
	}

	public Integer getLastBlockTransactions() {
		return lastBlockTransactions;
	}

	public Long getDifficulty() {
		return difficulty;
	}

	public Long getLastReforkTime() {
		return lastReforkTime;
	}

	public Long getNetworkHashRate() {
		return networkHashRate;
	}

	public Long getGasPrice() {
		return gasPrice;
	}

	public NetworkInfoDTO.SyncStatusDTO getSyncStatus() {
		return syncStatus;
	}
}
