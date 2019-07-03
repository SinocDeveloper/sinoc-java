package org.sinoc.net.eth.handler;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.tuple.Pair;
import org.sinoc.config.SystemProperties;
import org.sinoc.core.*;
import org.sinoc.datasource.Source;
import org.sinoc.db.BlockStore;
import org.sinoc.listener.CompositeEthereumListener;
import org.sinoc.net.eth.EthVersion;
import org.sinoc.net.eth.message.EthMessage;
import org.sinoc.net.eth.message.GetNodeDataMessage;
import org.sinoc.net.eth.message.GetReceiptsMessage;
import org.sinoc.net.eth.message.NodeDataMessage;
import org.sinoc.net.eth.message.ReceiptsMessage;
import org.sinoc.net.message.ReasonCode;
import org.sinoc.sync.PeerState;
import org.sinoc.util.ByteArraySet;
import org.sinoc.util.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.sinoc.crypto.HashUtil.sha3;
import static org.sinoc.net.eth.EthVersion.V63;
import static org.sinoc.util.ByteUtil.toHexString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Fast synchronization (PV63) Handler
 */
@Component("Eth63")
@Scope("prototype")
public class Eth63 extends Eth62 {

    private static final EthVersion version = V63;

    @Autowired @Qualifier("trieNodeSource")
    private Source<byte[], byte[]> trieNodeSource;

    private List<byte[]> requestedReceipts;
    private SettableFuture<List<List<TransactionReceipt>>> requestReceiptsFuture;
    private Set<byte[]> requestedNodes;
    private SettableFuture<List<Pair<byte[], byte[]>>> requestNodesFuture;

    public Eth63() {
        super(version);
    }

    @Autowired
    public Eth63(final SystemProperties config, final Blockchain blockchain, BlockStore blockStore,
                 final CompositeEthereumListener ethereumListener) {
        super(version, config, blockchain, blockStore, ethereumListener);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, EthMessage msg) throws InterruptedException {

        super.channelRead0(ctx, msg);

        // Only commands that were added in V63, V62 are handled in child
        switch (msg.getCommand()) {
            case GET_NODE_DATA:
                processGetNodeData((GetNodeDataMessage) msg);
                break;
            case NODE_DATA:
                processNodeData((NodeDataMessage) msg);
                break;
            case GET_RECEIPTS:
                processGetReceipts((GetReceiptsMessage) msg);
                break;
            case RECEIPTS:
                processReceipts((ReceiptsMessage) msg);
                break;
            default:
                break;
        }
    }

    protected synchronized void processGetNodeData(GetNodeDataMessage msg) {

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing GetNodeData, size [{}]",
                channel.getPeerIdShort(),
                msg.getNodeKeys().size()
        );

        List<Value> nodeValues = new ArrayList<>();
        for (byte[] nodeKey : msg.getNodeKeys()) {
            byte[] rawNode = trieNodeSource.get(nodeKey);
            if (rawNode != null) {
                Value value = new Value(rawNode);
                nodeValues.add(value);
                if (nodeValues.size() >= MAX_HASHES_TO_SEND) break;
                logger.trace("Eth63: " + toHexString(nodeKey).substring(0, 8) + " -> " + value);
            }
        }

        sendMessage(new NodeDataMessage(nodeValues));
    }

    protected synchronized void processGetReceipts(GetReceiptsMessage msg) {

        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing GetReceipts, size [{}]",
                channel.getPeerIdShort(),
                msg.getBlockHashes().size()
        );

        List<List<TransactionReceipt>> receipts = new ArrayList<>();
        int sizeSum = 0;
        for (byte[] blockHash : msg.getBlockHashes()) {
            Block block = blockchain.getBlockByHash(blockHash);
            if (block == null) continue;

            List<TransactionReceipt> blockReceipts = new ArrayList<>();
            for (Transaction transaction : block.getTransactionsList()) {
                TransactionInfo transactionInfo = blockchain.getTransactionInfo(transaction.getHash());
                if (transactionInfo == null) break;
                blockReceipts.add(transactionInfo.getReceipt());
                sizeSum += TransactionReceipt.MemEstimator.estimateSize(transactionInfo.getReceipt());
            }
            receipts.add(blockReceipts);
            if (sizeSum >= MAX_MESSAGE_SIZE) break;
        }

        sendMessage(new ReceiptsMessage(receipts));
    }

    public synchronized ListenableFuture<List<Pair<byte[], byte[]>>> requestTrieNodes(List<byte[]> hashes) {
        if (peerState != PeerState.IDLE) return null;

        GetNodeDataMessage msg = new GetNodeDataMessage(hashes);
        requestedNodes = new ByteArraySet();
        requestedNodes.addAll(hashes);

        requestNodesFuture = SettableFuture.create();
        sendMessage(msg);
        lastReqSentTime = System.currentTimeMillis();

        peerState = PeerState.NODE_RETRIEVING;
        return requestNodesFuture;
    }

    public synchronized ListenableFuture<List<List<TransactionReceipt>>> requestReceipts(List<byte[]> hashes) {
        if (peerState != PeerState.IDLE) return null;

        GetReceiptsMessage msg = new GetReceiptsMessage(hashes);
        requestedReceipts = hashes;
        peerState = PeerState.RECEIPT_RETRIEVING;

        requestReceiptsFuture = SettableFuture.create();
        sendMessage(msg);
        lastReqSentTime = System.currentTimeMillis();

        return requestReceiptsFuture;
    }

    protected synchronized void processNodeData(NodeDataMessage msg) {
        if (requestedNodes == null) {
            logger.debug("Received NodeDataMessage when requestedNodes == null. Dropping peer " + channel);
            dropConnection();
        }

        List<Pair<byte[], byte[]>> ret = new ArrayList<>();
        if(msg.getDataList().isEmpty()) {
            String err = String.format("Received NodeDataMessage contains empty node data. Dropping peer %s", channel);
            logger.debug(err);
            requestNodesFuture.setException(new RuntimeException(err));
            // Not fatal but let us touch it later
            channel.getChannelManager().disconnect(channel, ReasonCode.TOO_MANY_PEERS);
            return;
        }

        for (Value nodeVal : msg.getDataList()) {
            byte[] hash = sha3(nodeVal.asBytes());
            if (!requestedNodes.contains(hash)) {
                String err = "Received NodeDataMessage contains non-requested node with hash :" + toHexString(hash) + " . Dropping peer " + channel;
                dropUselessPeer(err);
                return;
            }
            ret.add(Pair.of(hash, nodeVal.asBytes()));
        }
        requestNodesFuture.set(ret);

        requestedNodes = null;
        requestNodesFuture = null;
        processingTime += (System.currentTimeMillis() - lastReqSentTime);
        lastReqSentTime = 0;
        peerState = PeerState.IDLE;
    }

    protected synchronized void processReceipts(ReceiptsMessage msg) {
        if (requestedReceipts == null) {
            logger.debug("Received ReceiptsMessage when requestedReceipts == null. Dropping peer " + channel);
            dropConnection();
        }


        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: processing Receipts, size [{}]",
                channel.getPeerIdShort(),
                msg.getReceipts().size()
        );

        List<List<TransactionReceipt>> receipts = msg.getReceipts();

        requestReceiptsFuture.set(receipts);

        requestedReceipts = null;
        requestReceiptsFuture = null;
        processingTime += (System.currentTimeMillis() - lastReqSentTime);
        lastReqSentTime = 0;
        peerState = PeerState.IDLE;
    }


    private void dropUselessPeer(String err) {
        logger.debug(err);
        requestNodesFuture.setException(new RuntimeException(err));
        dropConnection();
    }

    @Override
    public String getSyncStats() {
        double nodesPerSec = 1000d * channel.getNodeStatistics().eth63NodesReceived.get() / channel.getNodeStatistics().eth63NodesRetrieveTime.get();
        double missNodesRatio = 1 - (double) channel.getNodeStatistics().eth63NodesReceived.get() / channel.getNodeStatistics().eth63NodesRequested.get();
        long lifeTime = System.currentTimeMillis() - connectedTime;
        return super.getSyncStats() + String.format("\tNodes/sec: %1$.2f, miss: %2$.2f", nodesPerSec, missNodesRatio);
    }
}
