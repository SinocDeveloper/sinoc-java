package org.sinoc.shell.model.dto;

public class BlockInfo {

    private final long blockNumber;

    private final String blockHash;

    private final String parentHash;

    private final long difficulty;

	public BlockInfo(long blockNumber, String blockHash, String parentHash, long difficulty) {
		this.blockNumber = blockNumber;
		this.blockHash = blockHash;
		this.parentHash = parentHash;
		this.difficulty = difficulty;
	}

	public long getBlockNumber() {
		return blockNumber;
	}

	public String getBlockHash() {
		return blockHash;
	}

	public String getParentHash() {
		return parentHash;
	}

	public long getDifficulty() {
		return difficulty;
	}
}
