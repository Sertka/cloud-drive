package ru.stk.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import ru.stk.common.FileSender;
import ru.stk.common.MsgLib;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Class performs all communications with server
 * @version 1.0 17 Oct 2020
 * @author    Sergei Tkachev
 */
public class Client {

    public void connect() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Network.getInstance().start(networkStarter);
            }
        }).start();
        /* Waiting for connection to avoid early file sending */
        networkStarter.await();

    }

    /**
     * Gets file and call FileSender to send the file to sever
     */
    public void sendClientFile(Path filePath) throws IOException {
        FileSender.sendFile(filePath, Network.getInstance().getCurrentChannel(), null, new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл успешно передан");

                }
            }
        });
    }

    public void authClient (String login, String pass){

        ByteBuf buf = null;

        /* Sends command login and password - ATH means authorisation data */
        byte[] msgBytes = MsgLib.MsgType.ATH.toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(buf);
        //System.out.println(login + " " + pass);

        /*Send login */
        sendString(login);

        /*Send pass */
        sendString(pass);
    }

    /**
     * Sends incoming string to server
     * @param str - incoming string
     */
    private static void sendString(String str){
        ByteBuf buf = null;
        /*Sends string length */
        byte[] msgBytes = str.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(msgBytes.length);
        send(buf);

        /*Sends string */
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(buf);
    }

    private static void send(ByteBuf buf){
        Channel channel = Network.getInstance().getCurrentChannel();
        channel.writeAndFlush(buf);
    }
}
