package org.sinoc.net.server;

import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.sinoc.net.rlpx.discover.NodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class EthereumChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    ChannelManager channelManager;

    @Autowired
    NodeManager nodeManager;

    private String remoteId;

    private boolean peerDiscoveryMode = false;

    public EthereumChannelInitializer(String remoteId) {
        this.remoteId = remoteId;
    }

    @Override
    public void initChannel(NioSocketChannel ch) throws Exception {
        try {
            if (!peerDiscoveryMode) {
                logger.debug("Open {} connection, channel: {}", isInbound() ? "inbound" : "outbound", ch.toString());
            }

            if (notEligibleForIncomingConnection(ch)) {
                ch.disconnect();
                return;
            }

            final Channel channel = ctx.getBean(Channel.class);
            channel.setInetSocketAddress(ch.remoteAddress());
            channel.init(ch.pipeline(), remoteId, peerDiscoveryMode, channelManager);

            if(!peerDiscoveryMode) {
                channelManager.add(channel);
            }

            // limit the size of receiving buffer to 1024
            ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(256 * 1024));
            ch.config().setOption(ChannelOption.SO_RCVBUF, 256 * 1024);
            ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);

            // be aware of channel closing
            ch.closeFuture().addListener((ChannelFutureListener) future -> {
                if (!peerDiscoveryMode) {
                    channelManager.notifyDisconnect(channel);
                }
            });

        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
        }
    }

    /**
     * Tests incoming connection channel for usual abuse/attack vectors
     * @param ch    Channel
     * @return true if we should refuse this connection, otherwise false
     */
    private boolean notEligibleForIncomingConnection(NioSocketChannel ch) {
        if(!isInbound()) return false;
        // For incoming connection drop if..
        
        // Bad remote address
        if (ch.remoteAddress() == null) {
            logger.debug("Drop connection - bad remote address, channel: {}", ch.toString());
            return true;
        }
        // Drop if we have long waiting queue already
        if (!channelManager.acceptingNewPeers()) {
            logger.debug("Drop connection - many new peers are not processed, channel: {}", ch.toString());
            return true;
        }
        // Refuse connections from ips that are already in connection queue
        // Local and private network addresses are still welcome!
        if (!ch.remoteAddress().getAddress().isLoopbackAddress() &&
                !ch.remoteAddress().getAddress().isSiteLocalAddress() &&
                channelManager.isAddressInQueue(ch.remoteAddress().getAddress())) {
            logger.debug("Drop connection - already processing connection from this host, channel: {}", ch.toString());
            return true;
        }

        // Avoid too frequent connection attempts
        if (channelManager.isRecentlyDisconnected(ch.remoteAddress().getAddress())) {
            logger.debug("Drop connection - the same IP was disconnected recently, channel: {}", ch.toString());
            return true;
        }
        // Drop bad peers before creating channel
        if (nodeManager.isReputationPenalized(ch.remoteAddress())) {
            logger.debug("Drop connection - bad peer, channel: {}", ch.toString());
            return true;
        }

        return false;
    }

    private boolean isInbound() {
        return remoteId == null || remoteId.isEmpty();
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }
}
