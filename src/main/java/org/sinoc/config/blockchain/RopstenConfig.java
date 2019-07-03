package org.sinoc.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.sinoc.config.BlockchainConfig;
import org.sinoc.validator.BlockCustomHashRule;
import org.sinoc.validator.BlockHeaderRule;
import org.sinoc.validator.BlockHeaderValidator;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

public class RopstenConfig extends Eip160HFConfig {

    // Check for 1 known block to exclude fake peers
    private static final long CHECK_BLOCK_NUMBER = 10;
    private static final byte[] CHECK_BLOCK_HASH = Hex.decode("b3074f936815a0425e674890d7db7b5e94f3a06dca5b22d291b55dcd02dde93e");

    public RopstenConfig(BlockchainConfig parent) {
        super(parent);
        headerValidators().add(Pair.of(CHECK_BLOCK_NUMBER, new BlockHeaderValidator(new BlockCustomHashRule(CHECK_BLOCK_HASH))));
    }

    @Override
    public Integer getChainId() {
        return 3;
    }
}
