package org.sinoc.validator;

import org.sinoc.core.BlockHeader;

public class DependentBlockHeaderRuleAdapter extends DependentBlockHeaderRule {

    @Override
    public boolean validate(BlockHeader header, BlockHeader dependency) {
        return true;
    }
}
