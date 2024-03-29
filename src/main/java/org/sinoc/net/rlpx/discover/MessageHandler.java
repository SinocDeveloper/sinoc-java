package org.sinoc.net.rlpx.discover;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.function.Consumer;


public class MessageHandler extends SimpleChannelInboundHandler<DiscoveryEvent>
        implements Consumer<DiscoveryEvent> {
    static final org.slf4j.Logger logger = LoggerFactory.getLogger("discover");

    public Channel channel;

    NodeManager nodeManager;

    public MessageHandler(NioDatagramChannel ch, NodeManager nodeManager) {
        channel = ch;
        this.nodeManager = nodeManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        nodeManager.channelActivated();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DiscoveryEvent event) throws Exception {
        nodeManager.handleInbound(event);
    }

    @Override
    public void accept(DiscoveryEvent discoveryEvent) {
        InetSocketAddress address = discoveryEvent.getAddress();
        sendPacket(discoveryEvent.getMessage().getPacket(), address);
    }

    void sendPacket(byte[] wire, InetSocketAddress address) {
        DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(wire), address);
        channel.write(packet);
        channel.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("Discover channel error" + cause);
        ctx.close();
        // We don't close the channel because we can keep serving requests.
    }
}
