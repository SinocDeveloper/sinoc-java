package org.sinoc.config.blockchain;

import static org.sinoc.config.blockchain.HomesteadConfig.SECP256K1N_HALF;

import java.util.Objects;

import org.sinoc.config.BlockchainConfig;
import org.sinoc.config.Constants;
import org.sinoc.config.ConstantsAdapter;
import org.sinoc.core.Transaction;
import org.sinoc.vm.GasCost;

/**
 * Hard fork includes following EIPs:
 * EIP 155 - Simple replay attack protection
 * EIP 160 - EXP cost increase
 * EIP 161 - State trie clearing (invariant-preserving alternative)
 */
public class Eip160HFConfig extends Eip150HFConfig {

    static class GasCostEip160HF extends GasCostEip150HF {
        public int getEXP_BYTE_GAS()        {     return 50;     }
    }

    private static final GasCost NEW_GAS_COST = new GasCostEip160HF();

    private final Constants constants;

    public Eip160HFConfig(BlockchainConfig parent) {
        super(parent);
        constants = new ConstantsAdapter(parent.getConstants()) {
            @Override
            public int getMAX_CONTRACT_SZIE() {
                return 0x6000;
            }
        };
    }

    @Override
    public GasCost getGasCost() {
        return NEW_GAS_COST;
    }

    @Override
    public boolean eip161() {
        return true;
    }

    @Override
    public Integer getChainId() {
        return 1;
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {

        if (tx.getSignature() == null) return false;

        // Restoring old logic. Making this through inheritance stinks too much
        if (!tx.getSignature().validateComponents() ||
                tx.getSignature().s.compareTo(SECP256K1N_HALF) > 0) return false;
        return  tx.getChainId() == null || Objects.equals(getChainId(), tx.getChainId());
    }
}
