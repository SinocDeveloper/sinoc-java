package org.sinoc.validator;

import java.math.BigInteger;

import org.sinoc.config.Constants;
import org.sinoc.config.SystemProperties;
import org.sinoc.core.BlockHeader;

/**
 * Checks {@link BlockHeader#gasLimit} against {@link Constants#getMIN_GAS_LIMIT}. <br>
 *
 * This check is NOT run in Frontier
 */
public class GasLimitRule extends BlockHeaderRule {

    private final int MIN_GAS_LIMIT;

    public GasLimitRule(SystemProperties config) {
        MIN_GAS_LIMIT = config.getBlockchainConfig().
                getCommonConstants().getMIN_GAS_LIMIT();
    }

    @Override
    public ValidationResult validate(BlockHeader header) {

        if (new BigInteger(1, header.getGasLimit()).compareTo(BigInteger.valueOf(MIN_GAS_LIMIT)) < 0) {
            return fault("header.getGasLimit() < MIN_GAS_LIMIT");
        }

        return Success;
    }
}
