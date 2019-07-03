package org.sinoc.net.submit;

import org.sinoc.core.Transaction;

public class WalletTransaction {

    private final Transaction tx;
    private int approved = 0; // each time the tx got from the wire this value increased

    public WalletTransaction(Transaction tx) {
        this.tx = tx;
    }

    public void incApproved() {
        approved++;
    }

    public int getApproved() {
        return approved;
    }
}
