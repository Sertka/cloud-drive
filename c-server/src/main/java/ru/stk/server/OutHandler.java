package ru.stk.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import ru.stk.common.FileSender;
import ru.stk.common.MsgLib;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class OutHandler extends ChannelOutboundHandlerAdapter {
    private Channel curChannel;

    public void setChannel(Channel s){
        curChannel = s;
    };

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        String message;
        message = msg.toString();

        if (message.equals(MsgLib.MsgType.EMP.toString()) ) {
            sendMessage(message, ctx);
        }

       FileSender.sendFile(Paths.get(message), null, ctx, new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл успешно передан клиенту");

                }
            }
        });

    }
    private void sendMessage(String msg, ChannelHandlerContext ctx){

        ByteBuf buf = null;
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        ctx.writeAndFlush(buf);
    }


}
