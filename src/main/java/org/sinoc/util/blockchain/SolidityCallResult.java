package org.sinoc.util.blockchain;

import java.util.Arrays;
import java.util.List;

import org.sinoc.core.CallTransaction;

public abstract class SolidityCallResult extends TransactionResult {
    public Object getReturnValue() {
        Object[] returnValues = getReturnValues();
        return isIncluded() && returnValues.length > 0 ? returnValues[0] : null;
    }

    public Object[] getReturnValues() {
        if (!isIncluded()) return null;
        byte[] executionResult = getReceipt().getExecutionResult();
        return getFunction().decodeResult(executionResult);
    }

    public abstract CallTransaction.Function getFunction();

    public boolean isSuccessful() {
        return isIncluded() && getReceipt().isSuccessful();
    }

    public abstract List<CallTransaction.Invocation> getEvents();

    @Override
    public String toString() {
        String ret = "SolidityCallResult{" +
                getFunction() + ": " +
                (isIncluded() ? "EXECUTED" : "PENDING") + ", ";
        if (isIncluded()) {
            ret += isSuccessful() ? "SUCCESS" : ("ERR (" + getReceipt().getError() + ")");
            ret += ", ";
            if (isSuccessful()) {
                ret += "Ret: " + Arrays.toString(getReturnValues()) + ", ";
                ret += "Events: " + getEvents() + ", ";
            }
        }
        return ret + "}";
    }
}
