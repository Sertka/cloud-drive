package ru.stk.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import javafx.stage.Stage;
import ru.stk.common.FileSender;
import ru.stk.common.MsgLib;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class Handler uses Netty functionality to establish connections with clients and
 * handle commands from clients.
 */

public class MainHandler extends ChannelInboundHandlerAdapter {

    /*Defines current stage in communication with client */
    private enum State {
        IDLE,
        NAME_LEN, NAME, FILE_LEN, FILE,
        LOGIN_LEN, LOGIN, PASS_LEN, PASS
    }

    private State curState = State.IDLE;
    private int fileNameLength;
    private long fileLength;
    private long inFileLength;
    private int loginLength;
    private int passLength;
    private String login;
    private String pass;

    private BufferedOutputStream fileSaveStream;
    private byte[] msgBytes = new byte[MsgLib.MSG_LEN];
    private String msgString = "";

    @Override
    public void channelRead (ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf buf = ((ByteBuf) msg);
        curState = State.IDLE;

        while (buf.readableBytes() > 0) {
            /* TODO: Here establish call of different commands - Cmd and FileService */

            if (curState == State.IDLE){
                buf.readBytes(msgBytes);
                msgString = new String(msgBytes, "UTF-8");
            }
            /* Receive command of the incoming login and password - ATH means authorisation data */
            if (curState == State.IDLE & msgString.equals("ATH")){
                curState = State.LOGIN_LEN;
                inFileLength = 0L;
                System.out.println("STATE: Start auth receiving");
            } /* Receive length of a login */
            else if (curState == State.LOGIN_LEN) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get login length");
                    loginLength = buf.readInt();
                    curState = State.LOGIN;
                } /* Receive login */
            } else if (curState == State.LOGIN) {
                if (buf.readableBytes() >= loginLength) {
                    System.out.print("STATE: Get login: ");
                    byte[] loginBytes = new byte[loginLength];
                    buf.readBytes(loginBytes);
                    login = new String(loginBytes, "UTF-8");
                    System.out.println(login);
                    curState = State.PASS_LEN;
                } /* Receive length of a password */
            } else if (curState == State.PASS_LEN) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get pass length");
                    passLength = buf.readInt();
                    curState = State.PASS;
                }
            } /* Receive password */ else if (curState == State.PASS) {
                if (buf.readableBytes() >= passLength) {
                    System.out.print("STATE: Get pass: ");
                    byte[] passBytes = new byte[passLength];
                    buf.readBytes(passBytes);
                    pass = new String(passBytes, "UTF-8");
                    System.out.println(pass);
                    curState = State.IDLE;
                }
            }



            /* Receive command of the incoming new file - FLE means a file */
            if (curState == State.IDLE & msgString.equals("FLE")) {
                //buf.readBytes(msgBytes);
                //msgString = new String(msgBytes, "UTF-8");
                curState = State.NAME_LEN;
                inFileLength = 0L;
                System.out.println("STATE: Start file receiving");
            }

            /* Receive length of a new file name */
            if (curState == State.NAME_LEN) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    fileNameLength = buf.readInt();
                    curState = State.NAME;
                }
            }

            /* Receive file name and create a new file in user's directory */
            if (curState == State.NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] fileName = new byte[fileNameLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - _" + new String(fileName, "UTF-8"));
                    fileSaveStream = new BufferedOutputStream
                            (new FileOutputStream("_" + new String(fileName)));
                    curState = State.FILE_LEN;
                }
            }

            /* Receive length of a new file */
            if (curState == State.FILE_LEN) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    curState = State.FILE;
                }
            }

            /*TODO: Put the file to the valid directory of the user */
            /*TODO: Add error messages to the user and log for all blocks */
            /* Receive and save content of the file */
            if (curState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    fileSaveStream.write(buf.readByte());
                    inFileLength++;
                    if (fileLength == inFileLength) {
                        curState = State.IDLE;
                        System.out.println("File received and saved");
                        fileSaveStream.close();
                        break;
                    }

                }
            }
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
        ctx.writeAndFlush("HW04_CodeReview_S.Tkachev_Questions.txt");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
