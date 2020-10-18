package ru.stk.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class FileReceiver {
    /*Defines current stage in communication with client */
    public enum State {
        IDLE, NAME_LEN, NAME, FILE_LEN, FILE
    }

    private State curState = State.IDLE;
    private int newFileLength;
    private long fileLength;
    private long inFileLength;
    private BufferedOutputStream fileSaveStream;

    public static void receiveFile(ChannelHandlerContext ctx, Object msg) {

/*
        ByteBuf buf = ((ByteBuf) msg);
                while(buf.readableBytes()>0)
*/
/*


            */
/* Check if client wants to send new file - first byte = 25 *//*

            */
/* TODO: Here call of different commands - Cmd and FileService *//*

            if (curState == State.IDLE) {
                byte read = buf.readByte();
                if (read == (byte) 25) {
                    curState = State.NAME_LEN;
                    inFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + read);
                }
            }

            */
/* Receive length of a new file name *//*

            if (curState == State.NAME_LEN) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    newFileLength = buf.readInt();
                    curState = State.NAME;
                }
            }

            */
/* Create a new file in user's directory *//*

            */
/*TODO: Put the file to the valid directory of the user *//*

            */
/*TODO: Add error messages to the user and log for all blocks *//*

            if (curState == State.NAME) {
                if (buf.readableBytes() >= newFileLength) {
                    byte[] fileName = new byte[newFileLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received - _" + new String(fileName, "UTF-8"));
                    fileSaveStream = new BufferedOutputStream(new FileOutputStream("_" + new String(fileName)));
                    curState = State.FILE_LEN;
                }
            }

            */
/* Receive length of a new file *//*

            if (curState == State.FILE_LEN) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    curState = State.FILE;
                }
            }

            */
/* Receive and save content of the file *//*

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
*/

    }
}
