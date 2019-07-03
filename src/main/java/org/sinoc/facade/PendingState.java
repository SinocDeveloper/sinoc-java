package org.sinoc.facade;

import java.util.List;
import java.util.Set;

import org.sinoc.core.*;

public interface PendingState {

    /**
     * @return pending state repository
     */
    org.sinoc.core.Repository getRepository();

    /**
     * @return list of pending transactions
     */
    List<Transaction> getPendingTransactions();
}
