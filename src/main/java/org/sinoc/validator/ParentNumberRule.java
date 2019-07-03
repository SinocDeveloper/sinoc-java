package org.sinoc.validator;

import org.sinoc.core.BlockHeader;

/**
 * Checks if {@link BlockHeader#number} == {@link BlockHeader#number} + 1 of parent's block
 *
 */
public class ParentNumberRule extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader parent) {

        errors.clear();

        if (header.getNumber() != (parent.getNumber() + 1)) {
            errors.add(String.format("#%d: block number is not parentBlock number + 1", header.getNumber()));
            return false;
        }

        return true;
    }
}
