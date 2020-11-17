package ru.stk.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSender {
    public static void sendFile(Path path, Channel channel, ChannelHandlerContext ctx,ChannelFutureListener finishListener) throws IOException {

        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        ByteBuf buf = null;
        byte[] msgBytes = MsgLib.MsgType.FLE.toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(channel, ctx, buf);

        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        send(channel, ctx, buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        send(channel, ctx, buf);
        //channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        buf.writeLong(Files.size(path));
        send(channel, ctx, buf);
        //channel.writeAndFlush(buf);


        ChannelFuture transferOperationFuture;
        if (isServer (channel)){
            transferOperationFuture = ctx.writeAndFlush(region);
        }else {
            transferOperationFuture = channel.writeAndFlush(region);
        }
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    private static void send(Channel channel, ChannelHandlerContext ctx,  ByteBuf buf){
        if (isServer(channel)){
            ctx.writeAndFlush(buf);
        }else {
            channel.writeAndFlush(buf);
        }
    }

    private static boolean isServer(Channel channel){
        return channel == null;
    }
}
