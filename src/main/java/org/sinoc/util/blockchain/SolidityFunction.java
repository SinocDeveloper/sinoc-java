package org.sinoc.util.blockchain;

import org.sinoc.core.CallTransaction;

public interface SolidityFunction {

    SolidityContract getContract();

    CallTransaction.Function getInterface();
}
