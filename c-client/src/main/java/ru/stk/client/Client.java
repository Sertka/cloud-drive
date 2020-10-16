package ru.stk.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import ru.stk.common.FileSender;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class Client {

    public void connect() throws InterruptedException, IOException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Network.getInstance().start(networkStarter);
            }
        }).start();
        networkStarter.await();


        FileSender.sendFile(Paths.get("demo.txt"), Network.getInstance().getCurrentChannel(), new ChannelFutureListener() {
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
}
