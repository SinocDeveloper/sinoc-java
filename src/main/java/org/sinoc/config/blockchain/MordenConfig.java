package org.sinoc.config.blockchain;

import java.math.BigInteger;

public class MordenConfig {
    private static final BigInteger NONSE = BigInteger.valueOf(0x100000);

    public static class Frontier extends FrontierConfig {
        public Frontier() {
            super(new FrontierConstants() {
                @Override
                public BigInteger getInitialNonce() {
                    return NONSE;
                }
            });
        }
    }

    public static class Homestead extends HomesteadConfig {
        public Homestead() {
            super(new HomesteadConstants() {
                @Override
                public BigInteger getInitialNonce() {
                    return NONSE;
                }
            });
        }
    }
}
