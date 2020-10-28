package ru.stk.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import ru.stk.common.FileSender;
import ru.stk.common.MsgLib;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Handle and sends messages and files to user
 */
public class OutHandler extends ChannelOutboundHandlerAdapter {
    private Channel curChannel;

    public void setChannel(Channel s){
        curChannel = s;
    };

    /*
     * method checks messages and initiates sending via sendMessage call
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String message = msg.toString();

        if (msg.getClass().equals(File.class)) {
            FileSender.sendFile(Paths.get(message), null, ctx, new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();
                    }
                    if (future.isSuccess()) {
                        // sends message to user about successful download - if not .tmp file
                        if (message.length() > 3) {
                            String fileType = message.substring(message.length() - 3);
                            if (!fileType.equals("tmp")) {
                                sendMessage(MsgLib.MsgType.DLS.toString(), ctx);
                            }
                        }
                    }
                }
            });
        } else{
            sendMessage(message, ctx);
        }
    }

    /*
     * method sends bytes vie Netty chanel
     */
    private void sendMessage(String msg, ChannelHandlerContext ctx){

        ByteBuf buf = null;
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        ctx.writeAndFlush(buf);
    }
}