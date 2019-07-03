package org.sinoc.validator;

import java.math.BigInteger;

import org.sinoc.core.BlockHeader;

/**
 * Checks {@link BlockHeader#gasUsed} against {@link BlockHeader#gasLimit}
 * 
 */
public class GasValueRule extends BlockHeaderRule {

    @Override
    public ValidationResult validate(BlockHeader header) {
        if (new BigInteger(1, header.getGasLimit()).compareTo(BigInteger.valueOf(header.getGasUsed())) < 0) {
            return fault("header.getGasLimit() < header.getGasUsed()");
        }

        return Success;
    }
}
