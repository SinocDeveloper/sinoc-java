package org.sinoc.config.blockchain;

import java.math.BigInteger;

import org.sinoc.config.BlockchainConfig;
import org.sinoc.config.Constants;
import org.sinoc.config.ConstantsAdapter;
import org.sinoc.core.BlockHeader;

public class SingularityConfig extends Eip160HFConfig {

	private final Constants constants;

	public SingularityConfig(BlockchainConfig parent) {
		super(parent);
		constants = new ConstantsAdapter(super.getConstants()) {
			private final BigInteger BLOCK_REWARD = new BigInteger("180000000000000000000");

			@Override
			public BigInteger getBLOCK_REWARD() {
				return BLOCK_REWARD;
			}
		};
	}

	@Override
	public Constants getConstants() {
		return constants;
	}

	@Override
	public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
		if (curBlock.isGenesis()) {
			return curBlock.getDifficultyBI();
		} else {
			BigInteger result = parent.getDifficultyBI().add(new BigInteger("18446744073709551616")
					.divide(curBlock.getDeadLineBI()));
			return BigInteger.ZERO.compareTo(result)==0?BigInteger.ONE:result;
		}
	}

	@Override
	public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
		return BigInteger.ZERO;
	}

	@Override
	public boolean eip198() {
		return true;
	}

	@Override
	public boolean eip206() {
		return true;
	}

	@Override
	public boolean eip211() {
		return true;
	}

	@Override
	public boolean eip212() {
		return true;
	}

	@Override
	public boolean eip213() {
		return true;
	}

	@Override
	public boolean eip214() {
		return true;
	}

	@Override
	public boolean eip658() {
		return true;
	}
}
