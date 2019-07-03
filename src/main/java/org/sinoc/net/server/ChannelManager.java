package org.sinoc.net.server;

import org.apache.commons.collections4.map.LRUMap;
import org.sinoc.config.NodeFilter;
import org.sinoc.config.SystemProperties;
import org.sinoc.core.Block;
import org.sinoc.core.BlockWrapper;
import org.sinoc.core.PendingState;
import org.sinoc.core.Transaction;
import org.sinoc.db.ByteArrayWrapper;
import org.sinoc.facade.Ethereum;
import org.sinoc.net.message.ReasonCode;
import org.sinoc.net.rlpx.Node;
import org.sinoc.sync.SyncManager;
import org.sinoc.sync.SyncPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.sinoc.net.message.ReasonCode.DUPLICATE_PEER;
import static org.sinoc.net.message.ReasonCode.TOO_MANY_PEERS;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

@Component
public class ChannelManager {

    private static final Logger logger = LoggerFactory.getLogger("net");

    // If the inbound peer connection was dropped by us with a reason message
    // then we ban that peer IP on any connections for some time to protect from
    // too active peers
    public static final int INBOUND_CONNECTION_BAN_TIMEOUT = 120 * 1000;

    private List<Channel> newPeers = new CopyOnWriteArrayList<>();
    // Limiting number of new peers to avoid delays in processing
    private static final int MAX_NEW_PEERS = 128;
    private final Map<ByteArrayWrapper, Channel> activePeers = new ConcurrentHashMap<>();

    private ScheduledExecutorService mainWorker = Executors.newSingleThreadScheduledExecutor();
    private int maxActivePeers;
    private Map<InetAddress, Date> recentlyDisconnected = Collections.synchronizedMap(new LRUMap<InetAddress, Date>(500));
    private NodeFilter trustedPeers;

    /**
     * Queue with new blocks from other peers
     */
    private BlockingQueue<BlockWrapper> newForeignBlocks = new LinkedBlockingQueue<>();

    /**
     * Queue with new peers used for after channel init tasks
     */
    private BlockingQueue<Channel> newActivePeers = new LinkedBlockingQueue<>();

    private Thread blockDistributeThread;
    private Thread txDistributeThread;

    Random rnd = new Random();  // Used for distributing new blocks / hashes logic

    @Autowired
    SyncPool syncPool;

    @Autowired
    private Ethereum ethereum;

    @Autowired
    private PendingState pendingState;

    private SystemProperties config;

    private SyncManager syncManager;

    private PeerServer peerServer;

    @Autowired
    private ChannelManager(final SystemProperties config, final SyncManager syncManager,
                           final PeerServer peerServer) {
        this.config = config;
        this.syncManager = syncManager;
        this.peerServer = peerServer;
        maxActivePeers = config.maxActivePeers();
        trustedPeers = config.peerTrusted();
        mainWorker.scheduleWithFixedDelay(() -> {
            try {
                processNewPeers();
            } catch (Throwable t) {
                logger.error("Error", t);
            }
        }, 0, 1, TimeUnit.SECONDS);

        if (config.listenPort() > 0) {
            new Thread(() -> peerServer.start(config.listenPort()),
            "PeerServerThread").start();
        }

        // Resending new blocks to network in loop
        this.blockDistributeThread = new Thread(this::newBlocksDistributeLoop, "NewSyncThreadBlocks");
        this.blockDistributeThread.start();

        // Resending pending txs to newly connected peers
        this.txDistributeThread = new Thread(this::newTxDistributeLoop, "NewPeersThread");
        this.txDistributeThread.start();
    }

    public void connect(Node node) {
        if (logger.isTraceEnabled()) logger.trace(
                "Peer {}: initiate connection",
                node.getHexIdShort()
        );
        if (nodesInUse().contains(node.getHexId())) {
            if (logger.isTraceEnabled()) logger.trace(
                    "Peer {}: connection already initiated",
                    node.getHexIdShort()
            );
            return;
        }

        ethereum.connect(node);
    }

    public Set<String> nodesInUse() {
        Set<String> ids = new HashSet<>();
        for (Channel peer : getActivePeers()) {
            ids.add(peer.getPeerId());
        }
        for (Channel peer : newPeers) {
            ids.add(peer.getPeerId());
        }
        return ids;
    }

    private void processNewPeers() {
        List<Runnable> noLockTasks = new ArrayList<>();

        synchronized (this) {
            if (newPeers.isEmpty()) return;

            List<Channel> processed = new ArrayList<>();
            int addCnt = 0;
            for (Channel peer : newPeers) {

                logger.debug("Processing new peer: " + peer);

                if (peer.isProtocolsInitialized()) {

                    logger.debug("Protocols initialized");

                    if (!activePeers.containsKey(peer.getNodeIdWrapper())) {
                        if (!peer.isActive() &&
                                activePeers.size() >= maxActivePeers &&
                                !trustedPeers.accept(peer.getNode())) {

                            // restricting inbound connections unless this is a trusted peer

                            noLockTasks.add(() -> disconnect(peer, TOO_MANY_PEERS));
                        } else {
                            addCnt++;
                            process(peer);
                        }
                    } else {
                        noLockTasks.add(() -> disconnect(peer, DUPLICATE_PEER));
                    }

                    processed.add(peer);
                }
            }

            if (addCnt > 0) {
                logger.info("New peers processed: " + processed + ", active peers added: " + addCnt + ", total active peers: " + activePeers.size());
            }

            newPeers.removeAll(processed);
        }

        noLockTasks.forEach(Runnable::run);
    }

    public void disconnect(Channel peer, ReasonCode reason) {
        logger.debug("Disconnecting peer with reason " + reason + ": " + peer);
        peer.disconnect(reason);
        recentlyDisconnected.put(peer.getInetSocketAddress().getAddress(), new Date());
    }

    /**
     * Whether peer with the same ip is in newPeers, waiting for processing
     * @param peerAddr      Peer address
     * @return true if we already have connection from this address, otherwise false
     */
    public boolean isAddressInQueue(InetAddress peerAddr) {
        for (Channel peer: newPeers) {
            if (peer.getInetSocketAddress() != null &&
                    peer.getInetSocketAddress().getAddress().getHostAddress().equals(peerAddr.getHostAddress())) {
                return true;
            }
        }

        return false;
    }

    public boolean isRecentlyDisconnected(InetAddress peerAddr) {
        Date disconnectTime = recentlyDisconnected.get(peerAddr);
        if (disconnectTime != null &&
                System.currentTimeMillis() - disconnectTime.getTime() < INBOUND_CONNECTION_BAN_TIMEOUT) {
            return true;
        } else {
            recentlyDisconnected.remove(peerAddr);
            return false;
        }
    }

    private void process(Channel peer) {
        if(peer.hasEthStatusSucceeded()) {
            // prohibit transactions processing until main sync is done
            if (syncManager.isSyncDone()) {
                peer.onSyncDone(true);
                // So we could perform some tasks on recently connected peer
                newActivePeers.add(peer);
            }
            activePeers.put(peer.getNodeIdWrapper(), peer);
        }
    }

    /**
     * Propagates the transactions message across active peers with exclusion of
     * 'receivedFrom' peer.
     * @param txs  transactions to be sent
     * @param receivedFrom the peer which sent original message or null if
     *                     the transactions were originated by this peer
     */
    public void sendTransaction(List<Transaction> txs, Channel receivedFrom) {
        for (Channel channel : activePeers.values()) {
            if (channel != receivedFrom) {
                channel.sendTransactionsCapped(txs);
            }
        }
    }

    /**
     * Propagates the new block message across active peers
     * Suitable only for self-mined blocks
     * Use {@link #sendNewBlock(Block, Channel)} for sending blocks received from net
     * @param block  new Block to be sent
     */
    public void sendNewBlock(Block block) {
        for (Channel channel : activePeers.values()) {
            channel.sendNewBlock(block);
        }
    }

    /**
     * Called on new blocks received from other peers
     * @param blockWrapper  Block with additional info
     */
    public void onNewForeignBlock(BlockWrapper blockWrapper) {
        newForeignBlocks.add(blockWrapper);
    }

    /**
     * Processing new blocks received from other peers from queue
     */
    private void newBlocksDistributeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            BlockWrapper wrapper = null;
            try {
                wrapper = newForeignBlocks.take();
                Channel receivedFrom = getActivePeer(wrapper.getNodeId());
                sendNewBlock(wrapper.getBlock(), receivedFrom);
            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                if (wrapper != null) {
                    logger.error("Error broadcasting new block {}: ", wrapper.getBlock().getShortDescr(), e);
                    logger.error("Block dump: {}", wrapper.getBlock());
                } else {
                    logger.error("Error broadcasting unknown block", e);
                }
            }
        }
    }

    /**
     * Sends all pending txs to new active peers
     */
    private void newTxDistributeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            Channel channel = null;
            try {
                channel = newActivePeers.take();
                List<Transaction> pendingTransactions = pendingState.getPendingTransactions();
                if (!pendingTransactions.isEmpty()) {
                    channel.sendTransactionsCapped(pendingTransactions);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Throwable e) {
                if (channel != null) {
                    logger.error("Error sending transactions to peer {}: ", channel.getNode().getHexIdShort(), e);
                } else {
                    logger.error("Unknown error when sending transactions to new peer", e);
                }
            }
        }
    }

    /**
     * Propagates the new block message across active peers with exclusion of
     * 'receivedFrom' peer.
     * Distributes full block to 30% of peers and only its hash to remains
     * @param block  new Block to be sent
     * @param receivedFrom the peer which sent original message
     */
    private void sendNewBlock(Block block, Channel receivedFrom) {
        for (Channel channel : activePeers.values()) {
            if (channel == receivedFrom) continue;
            if (rnd.nextInt(10) < 3) {  // 30%
                channel.sendNewBlock(block);
            } else {                    // 70%
                channel.sendNewBlockHashes(block);
            }
        }
    }

    public synchronized void add(Channel peer) {
        logger.debug("New peer in ChannelManager {}", peer);
        newPeers.add(peer);
    }

    public void notifyDisconnect(Channel channel) {
        logger.debug("Peer {}: notifies about disconnect", channel);
        channel.onDisconnect();
        syncPool.onDisconnect(channel);
        synchronized(this) {
            activePeers.values().remove(channel);
            newPeers.remove(channel);
        }
    }

    public void onSyncDone(boolean done) {
        for (Channel channel : activePeers.values())
            channel.onSyncDone(done);
    }

    public Collection<Channel> getActivePeers() {
        return new ArrayList<>(activePeers.values());
    }

    /**
     * Checks whether newPeers is not full
     * newPeers are used to fill up active peers
     * @return True if there are free slots for new peers
     */
    public boolean acceptingNewPeers() {
        return newPeers.size() < Math.max(config.maxActivePeers(), MAX_NEW_PEERS);
    }

    public Channel getActivePeer(byte[] nodeId) {
        return activePeers.get(new ByteArrayWrapper(nodeId));
    }

    public SyncManager getSyncManager() {
        return syncManager;
    }

    public void close() {
        try {
            logger.info("Shutting down block and tx distribute threads...");
            if (blockDistributeThread != null) blockDistributeThread.interrupt();
            if (txDistributeThread != null) txDistributeThread.interrupt();

            logger.info("Shutting down ChannelManager worker thread...");
            mainWorker.shutdownNow();
            mainWorker.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Problems shutting down", e);
        }
        peerServer.close();

        ArrayList<Channel> allPeers = new ArrayList<>(activePeers.values());
        allPeers.addAll(newPeers);

        for (Channel channel : allPeers) {
            try {
                channel.dropConnection();
            } catch (Exception e) {
                logger.warn("Problems disconnecting channel " + channel, e);
            }
        }
    }
}
