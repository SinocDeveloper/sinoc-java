package org.sinoc.net.submit;

import org.sinoc.core.Transaction;
import org.sinoc.net.server.Channel;
import org.sinoc.net.server.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;

public class TransactionTask implements Callable<List<Transaction>> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private final List<Transaction> txs;
    private final ChannelManager channelManager;
    private final Channel receivedFrom;

    public TransactionTask(Transaction tx, ChannelManager channelManager) {
        this(Collections.singletonList(tx), channelManager);
    }

    public TransactionTask(List<Transaction> txs, ChannelManager channelManager) {
        this(txs, channelManager, null);
    }

    public TransactionTask(List<Transaction> txs, ChannelManager channelManager, Channel receivedFrom) {
        this.txs = txs;
        this.channelManager = channelManager;
        this.receivedFrom = receivedFrom;
    }

    @Override
    public List<Transaction> call() throws Exception {

        try {
            logger.info("submit txs: {}", txs.toString());
            channelManager.sendTransaction(txs, receivedFrom);
            return txs;

        } catch (Throwable th) {
            logger.warn("Exception caught: {}", th);
        }
        return null;
    }
}
