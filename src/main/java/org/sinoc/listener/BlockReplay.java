package org.sinoc.listener;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.sinoc.core.*;
import org.sinoc.db.BlockStore;
import org.sinoc.db.TransactionStore;
import org.sinoc.net.eth.message.StatusMessage;
import org.sinoc.net.message.Message;
import org.sinoc.net.p2p.HelloMessage;
import org.sinoc.net.rlpx.Node;
import org.sinoc.net.server.Channel;
import org.sinoc.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.sinoc.sync.BlockDownloader.MAX_IN_REQUEST;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Class capable of replaying stored blocks prior to 'going online' and
 * notifying on newly imported blocks
 *
 * All other EthereumListener events are just forwarded to the supplied listener.
 *
 * For example of usage, look at {@link org.sinoc.samples.EventListenerSample}
 *
 */
public class BlockReplay extends EthereumListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger("events");
    private static final int HALF_BUFFER = MAX_IN_REQUEST;

    BlockStore blockStore;
    TransactionStore transactionStore;

    EthereumListener listener;

    long firstBlock;

    boolean replayComplete = false;
    Block lastReplayedBlock;
    CircularFifoQueue<BlockSummary> onBlockBuffer = new CircularFifoQueue<>(HALF_BUFFER * 2);

    public BlockReplay(BlockStore blockStore, TransactionStore transactionStore, EthereumListener listener, long firstBlock) {
        this.blockStore = blockStore;
        this.transactionStore = transactionStore;
        this.listener = listener;
        this.firstBlock = firstBlock;
    }

    /**
     * Replay blocks asynchronously
     */
    public void replayAsync() {
        new Thread(this::replay).start();
    }

    /**
     * Replay blocks synchronously
     */
    public void replay() {
        long lastBlock = blockStore.getMaxNumber();
        logger.info("Replaying blocks from " + firstBlock + ", current best block: " + lastBlock);
        int cnt = 0;
        long num = firstBlock;
        while(!replayComplete) {
            for (; num <= lastBlock; num++) {
                replayBlock(num);
                cnt++;
                if (cnt % 1000 == 0) {
                    logger.info("Replayed " + cnt + " blocks so far. Current block: " + num);
                }
            }

            synchronized (this) {
                if (onBlockBuffer.size() < onBlockBuffer.maxSize()) {
                    replayComplete = true;
                } else {
                    // So we'll have half of the buffer for new blocks until not synchronized replay finish
                    long newLastBlock = blockStore.getMaxNumber() - HALF_BUFFER;
                    if (lastBlock >= newLastBlock) {
                        replayComplete = true;
                    } else {
                        lastBlock = newLastBlock;
                    }
                }
            }
        }
        logger.info("Replay complete.");
    }

    private void replayBlock(long num) {
        Block block = blockStore.getChainBlockByNumber(num);
        lastReplayedBlock = block;
        List<TransactionReceipt> receipts = new ArrayList<>();
        for (Transaction tx : block.getTransactionsList()) {
            TransactionInfo info = transactionStore.get(tx.getHash(), block.getHash());
            TransactionReceipt receipt = info.getReceipt();
            receipt.setTransaction(tx);
            receipts.add(receipt);
        }
        BlockSummary blockSummary = new BlockSummary(block, null, receipts, null);
        blockSummary.setTotalDifficulty(BigInteger.valueOf(num));
        listener.onBlock(blockSummary);
    }

    @Override
    public synchronized void onBlock(BlockSummary blockSummary) {
        if (replayComplete) {
            if (onBlockBuffer.isEmpty()) {
                listener.onBlock(blockSummary);
            } else {
                logger.info("Replaying cached " + onBlockBuffer.size() + " blocks...");
                boolean lastBlockFound = lastReplayedBlock == null || onBlockBuffer.size() < onBlockBuffer.maxSize();
                for (BlockSummary block : onBlockBuffer) {
                    if (!lastBlockFound) {
                        lastBlockFound = FastByteComparisons.equal(block.getBlock().getHash(), lastReplayedBlock.getHash());
                    } else {
                        listener.onBlock(block);
                    }
                }
                onBlockBuffer.clear();
                listener.onBlock(blockSummary);
                logger.info("Cache replay complete. Switching to online mode.");
            }
        } else {
            onBlockBuffer.add(blockSummary);
        }
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt transactionReceipt, PendingTransactionState pendingTransactionState, Block block) {
        listener.onPendingTransactionUpdate(transactionReceipt, pendingTransactionState, block);
    }

    @Override
    public void onPeerDisconnect(String s, long l) {
        listener.onPeerDisconnect(s, l);
    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> list) {
        listener.onPendingTransactionsReceived(list);
    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {
        listener.onPendingStateChanged(pendingState);
    }

    @Override
    public void onSyncDone(SyncState state) {
        listener.onSyncDone(state);
    }

    @Override
    public void onNoConnections() {
        listener.onNoConnections();
    }

    @Override
    public void onVMTraceCreated(String s, String s1) {
        listener.onVMTraceCreated(s, s1);
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary transactionExecutionSummary) {
        listener.onTransactionExecuted(transactionExecutionSummary);
    }

    @Override
    public void onPeerAddedToSyncPool(Channel channel) {
        listener.onPeerAddedToSyncPool(channel);
    }

    @Override
    public void trace(String s) {
        listener.trace(s);
    }

    @Override
    public void onNodeDiscovered(Node node) {
        listener.onNodeDiscovered(node);
    }

    @Override
    public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {
        listener.onHandShakePeer(channel, helloMessage);
    }

    @Override
    public void onEthStatusUpdated(Channel channel, StatusMessage statusMessage) {
        listener.onEthStatusUpdated(channel, statusMessage);
    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {
        listener.onRecvMessage(channel, message);
    }

    @Override
    public void onSendMessage(Channel channel, Message message) {
        listener.onSendMessage(channel, message);
    }
}
