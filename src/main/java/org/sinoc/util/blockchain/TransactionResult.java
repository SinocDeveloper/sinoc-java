package org.sinoc.util.blockchain;

import org.sinoc.core.TransactionExecutionSummary;
import org.sinoc.core.TransactionReceipt;

public class TransactionResult {
    TransactionReceipt receipt;
    TransactionExecutionSummary executionSummary;

    public boolean isIncluded() {
        return receipt != null;
    }

    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public TransactionExecutionSummary getExecutionSummary() {
        return executionSummary;
    }
}
