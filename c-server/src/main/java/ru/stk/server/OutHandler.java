package ru.stk.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.stk.common.FileSender;

import java.nio.channels.SocketChannel;
import java.nio.file.Paths;

public class OutHandler extends ChannelOutboundHandlerAdapter {
    private Channel curChannel;

    public void setChannel(Channel s){
        curChannel = s;
    };

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //String path = (String)msg;
/*
        String str = (String)msg;
        byte[] arr = str.getBytes();
        ByteBuf buf = ctx.alloc().buffer(arr.length);
        buf.writeBytes(arr);
        ctx.writeAndFlush(buf);*/

       FileSender.sendFile(Paths.get("server.txt"), null, ctx, new ChannelFutureListener() {
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

}
