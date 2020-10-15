package ru.stk.server;

import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Class Handler uses Netty functionality to establish connections with clients
 */

public class MainHandler extends ChannelInboundHandlerAdapter {
    /*Server state defines current stage in communication with client */
    public enum State {
        IDLE, NAME_LEN, NAME, FILE_LEN, FILE
    }


}
