package ru.stk.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.concurrent.CountDownLatch;

import javafx.scene.Scene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.common.Settings;
import ru.stk.gui.LoginFxCtl;
import ru.stk.gui.MainFxCtl;
import ru.stk.gui.MsgBox;


/**
 * Class provides Netty connection with server
 */
public class Network {
    private Channel curChannel;

    private static final Logger logger = LogManager.getLogger(Client.class);

    private static final Network thisNetwork = new Network();

    public static Network getInstance() {
        return thisNetwork;
    }

    public Channel getCurrentChannel() {
        return curChannel;
    }

    public void start(CountDownLatch countDownLatch, LoginFxCtl lfx, MainFxCtl mfx, Scene mainScene) throws Exception {
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            Bootstrap cBootstrap = new Bootstrap();

            cBootstrap.group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", Settings.PORT))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new ClientInHandler(lfx, mfx));
                            curChannel = socketChannel;
                        }
                    });
            ChannelFuture channelFuture = cBootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();

        } finally {
            try {
                clientGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                logger.error("Connection closing failure - " + e.getMessage());
            }
        }
    }

    public void stop() {
        curChannel.close();
    }
}

