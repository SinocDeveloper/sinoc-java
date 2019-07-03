package org.sinoc.config.blockchain;

import org.apache.commons.lang3.ArrayUtils;
import org.sinoc.config.Constants;
import org.sinoc.core.BlockHeader;
import org.sinoc.core.Transaction;

import java.math.BigInteger;

public class HomesteadConfig extends FrontierConfig {

    static final BigInteger SECP256K1N_HALF = Constants.getSECP256K1N().divide(BigInteger.valueOf(2));

    public static class HomesteadConstants extends FrontierConstants {
        @Override
        public boolean createEmptyContractOnOOG() {
            return false;
        }

        @Override
        public boolean hasDelegateCallOpcode() {
            return true;
        }
    };

    public HomesteadConfig() {
        this(new HomesteadConstants());
    }

    public HomesteadConfig(Constants constants) {
        super(constants);
    }

    @Override
    public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return BigInteger.valueOf(Math.max(1 - (curBlock.getTimestamp() - parent.getTimestamp()) / 10, -99));
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        long nonZeroes = tx.nonZeroDataBytes();
        long zeroVals  = ArrayUtils.getLength(tx.getData()) - nonZeroes;

        return (tx.isContractCreation() ? getGasCost().getTRANSACTION_CREATE_CONTRACT() : getGasCost().getTRANSACTION())
                + zeroVals * getGasCost().getTX_ZERO_DATA() + nonZeroes * getGasCost().getTX_NO_ZERO_DATA();
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        if (!super.acceptTransactionSignature(tx)) return false;
        if (tx.getSignature() == null) return false;
        return tx.getSignature().s.compareTo(SECP256K1N_HALF) <= 0;
    }
}
