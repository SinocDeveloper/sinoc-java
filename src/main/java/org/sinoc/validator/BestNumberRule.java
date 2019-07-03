package org.sinoc.validator;

import org.sinoc.config.Constants;
import org.sinoc.config.SystemProperties;
import org.sinoc.core.BlockHeader;

/**
 * Checks diff between number of some block and number of our best block. <br>
 * The diff must be more than -1 * {@link Constants#getBEST_NUMBER_DIFF_LIMIT}
 *
 */
public class BestNumberRule extends DependentBlockHeaderRule {

    private final int BEST_NUMBER_DIFF_LIMIT;

    public BestNumberRule(SystemProperties config) {
        BEST_NUMBER_DIFF_LIMIT = config.getBlockchainConfig().
                getCommonConstants().getBEST_NUMBER_DIFF_LIMIT();
    }

    @Override
    public boolean validate(BlockHeader header, BlockHeader bestHeader) {

        errors.clear();

        long diff = header.getNumber() - bestHeader.getNumber();

        if (diff > -1 * BEST_NUMBER_DIFF_LIMIT) {
            errors.add(String.format(
                    "#%d: (header.getNumber() - bestHeader.getNumber()) <= BEST_NUMBER_DIFF_LIMIT",
                    header.getNumber()
            ));
            return false;
        }

        return true;
    }
}
