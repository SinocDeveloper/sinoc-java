package org.sinoc.validator;

import java.math.BigInteger;

import org.sinoc.config.SystemProperties;
import org.sinoc.core.BlockHeader;

/**
 * Checks if {@link BlockHeader#gasLimit} matches gas limit bounds. <br>
 *
 * This check is NOT run in Frontier
 *
 */
public class ParentGasLimitRule extends DependentBlockHeaderRule {

    private final int GAS_LIMIT_BOUND_DIVISOR;

    public ParentGasLimitRule(SystemProperties config) {
        GAS_LIMIT_BOUND_DIVISOR = config.getBlockchainConfig().
                getCommonConstants().getGAS_LIMIT_BOUND_DIVISOR();
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();
        BigInteger headerGasLimit = new BigInteger(1, header.getGasLimit());
        BigInteger parentGasLimit = new BigInteger(1, parent.getGasLimit());

        if (headerGasLimit.compareTo(parentGasLimit.multiply(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR - 1)).divide(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR))) < 0 ||
            headerGasLimit.compareTo(parentGasLimit.multiply(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR + 1)).divide(BigInteger.valueOf(GAS_LIMIT_BOUND_DIVISOR))) > 0) {

            errors.add(String.format(
                    "#%d: gas limit exceeds parentBlock.getGasLimit() (+-) GAS_LIMIT_BOUND_DIVISOR",
                    header.getNumber()
            ));
            return false;
        }

        return true;
    }
}
