package org.sinoc.config.blockchain;

import java.math.BigInteger;

import org.sinoc.config.BlockchainConfig;
import org.sinoc.config.Constants;
import org.sinoc.config.ConstantsAdapter;

public class FutureCityConfig extends SingularityConfig {
	
	private final Constants constants;
	
	public FutureCityConfig(BlockchainConfig parent) {
		super(parent);
		constants = new ConstantsAdapter(super.getConstants()) {
			private final BigInteger BLOCK_REWARD = new BigInteger("720000000000000000000");

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
	
}
