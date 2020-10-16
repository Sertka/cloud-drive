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
import java.util.concurrent.CountDownLatch;

public class Network {

    private Network() {
    }

    private static final Network thisNetwork = new Network();

    public static Network getInstance() {
        return thisNetwork;
    }

    private Channel curChannel;

    public Channel getCurrentChannel() {
        return curChannel;
    }

    public void start(CountDownLatch countDownLatch) {
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            Bootstrap cBootstrap = new Bootstrap();
            cBootstrap.group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 8190))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast();
                            curChannel = socketChannel;
                        }
                    });
            ChannelFuture channelFuture = cBootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            /* TODO: Handle this exception, write log, inform user */
            e.printStackTrace();
        } finally {
            try {
                clientGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                /* TODO: Handle this exception, write log, inform user */
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        curChannel.close();
    }
}

