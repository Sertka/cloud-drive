package ru.stk.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.stk.common.MsgLib;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Class Handler uses Netty functionality to establish connections with clients and
 * handle commands from clients.
 */

public class MainHandler extends ChannelInboundHandlerAdapter {
    private AuthController authCtl;
    
    public MainHandler (AuthController autCtl){
        this. authCtl = autCtl;
    }

    /*Defines current stage in communication with client */
    private enum State {
        IDLE,
        NAME_LEN, NAME, FILE_LEN, FILE,
        DNL_LEN, DNL_NAME,
        LOGIN_LEN, LOGIN, PASS_LEN, PASS,
        OLD_NAME_LEN, OLD_NAME, NEW_NAME_LEN, NEW_NAME,
        DEL_LEN, DEL_NAME;
    }

    private State curState = State.IDLE;
    private int fileNameLength;
    private long fileLength;
    private String fileName;
    private long inFileLength;

    private int loginLength;
    private int passLength;
    private String login;
    private String pass;
    private String userPath;

    private String oldFileName;
    private String newFileName;

    private BufferedOutputStream fileSaveStream;
    private final byte[] msgBytes = new byte[MsgLib.MSG_LEN];
    private String msgString = "";

    @Override
    public void channelRead (ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf buf = ((ByteBuf) msg);
        File f;

        while (buf.readableBytes() > 0) {

            if (curState == State.IDLE) {
                buf.readBytes(msgBytes);
                msgString = new String(msgBytes, StandardCharsets.UTF_8);
            }
            /* Receive command of the incoming login and password - ATH means authorisation data */
            if (curState == State.IDLE & msgString.equals("ATH")) {
                curState = State.LOGIN_LEN;
                System.out.println("STATE: Start auth receiving");
            }
            /* Receive length of a login */
            if (curState == State.LOGIN_LEN) {
                if (buf.readableBytes() >= 4) {
                    loginLength = buf.readInt();
                    curState = State.LOGIN;
                }
            }
            // Receive login
            if (curState == State.LOGIN) {
                if (buf.readableBytes() >= loginLength) {
                    byte[] loginBytes = new byte[loginLength];
                    buf.readBytes(loginBytes);
                    login = new String(loginBytes, StandardCharsets.UTF_8);
                    curState = State.PASS_LEN;
                }
            }
            // Receive password length
            if (curState == State.PASS_LEN) {
                if (buf.readableBytes() >= 4) {
                    passLength = buf.readInt();
                    curState = State.PASS;
                }
            }
            // Receive password
            if (curState == State.PASS) {
                if (buf.readableBytes() >= passLength) {
                    byte[] passBytes = new byte[passLength];
                    buf.readBytes(passBytes);
                    pass = new String(passBytes, StandardCharsets.UTF_8);
                    curState = State.IDLE;
                }

                /* check user credentials and save user folder */
                String authResult = CmdService.clientLogin(login, pass, authCtl);
                if (authResult.equals("")) {
                    System.out.println("Authorisation failed");
                } else {
                    userPath = authResult;
                    System.out.println("Authorisation successful");
                    curState = State.IDLE;
                    //send storage content to user
                    f = CmdService.getFolderContent(userPath, login);
                    if (f == null){
                        ctx.writeAndFlush(MsgLib.MsgType.EMP);
                    } else {
                        ctx.writeAndFlush(f);
                    }
                }
            }


            // Receive command of the incoming new file - FLE means a file */
            if (curState == State.IDLE & msgString.equals("FLE")) {
                curState = State.NAME_LEN;
                inFileLength = 0L;
                System.out.println("STATE: Start file receiving");
            }

            /* Receive length of a new file name */
            if (curState == State.NAME_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.NAME;
                    System.out.println("STATE: Get filename length");
                }
            }

            // Receive file name and create a new file in user's directory */
            if (curState == State.NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    fileName = new String(nameBytes, StandardCharsets.UTF_8);
                    System.out.println("STATE: Filename received -" + fileName);
                    fileSaveStream = new BufferedOutputStream
                            (new FileOutputStream(userPath + fileName));
                    curState = State.FILE_LEN;
                }
            }

            // Receive length of a new file
            if (curState == State.FILE_LEN) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    curState = State.FILE;
                }
            }

            //TODO: Add error messages to the user and log for all blocks

            // Receive and save content of the file
            if (curState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    fileSaveStream.write(buf.readByte());
                    inFileLength++;
                    if (fileLength == inFileLength) {
                        curState = State.IDLE;
                        fileSaveStream.close();
                        System.out.println("File received and saved");

                        //send storage content to user
                        f = CmdService.getFolderContent(userPath, login);
                        if (f == null){
                            ctx.writeAndFlush(MsgLib.MsgType.EMP);
                        } else {
                            ctx.writeAndFlush(f);
                        }
                        break;
                    }
                }
            }

            // Receive command of the file download request
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.DNL.toString())) {
                curState = State.DNL_LEN;
            }
            // Receive length of a file name
            if (curState == State.DNL_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.DNL_NAME;
                    System.out.println("STATE: Download file name length received");
                }
            }
            // Receive file name
            if (curState == State.DNL_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    fileName = new String(nameBytes, StandardCharsets.UTF_8);
                    curState = State.IDLE;
                }
                System.out.println("Start file sending");
                ctx.writeAndFlush(userPath + fileName);
                //send storage content to user
                f = CmdService.getFolderContent(userPath, login);
                if (f == null){
                    ctx.writeAndFlush(MsgLib.MsgType.EMP);
                } else {
                    ctx.writeAndFlush(f);
                }
            }

            /*
             * This block is responsible for the renaming of the file
             * It gets old and new file names and calls renameFile method
             */

            /* Receive command of the incoming login and password - ATH means authorisation data */
            if (curState == State.IDLE & msgString.equals("REN")) {
                curState = State.OLD_NAME_LEN;
            }
            // Receive length of an old file name
            if (curState == State.OLD_NAME_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.OLD_NAME;
                }
            }
            // Receive old name
            if (curState == State.OLD_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    oldFileName = new String(nameBytes, StandardCharsets.UTF_8);
                    System.out.println("STATE: Get old file name");
                    curState = State.NEW_NAME_LEN;
                }
            }
            // Receive length of a new file name
            if (curState == State.NEW_NAME_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.NEW_NAME;
                }
            }
            // Receive new name
            if (curState == State.NEW_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    newFileName = new String(nameBytes, StandardCharsets.UTF_8);
                    System.out.println("STATE: Get new file name");
                    curState = State.IDLE;
                }

                //Rename file
                boolean renameResult = CmdService.renameFile(oldFileName, newFileName, login);
                if (renameResult) {
                    System.out.println("Rename successful");
                    //send storage content to user
                    f = CmdService.getFolderContent(userPath, login);
                    if (f == null){
                        ctx.writeAndFlush(MsgLib.MsgType.EMP);
                    } else {
                        ctx.writeAndFlush(f);
                    }
                } else {
                    System.out.println("Rename failed");
                }
            }

            /*
             * This block is responsible for the deleting of a file
             */
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.DEL.toString())) {
                curState = State.DEL_LEN;
            }
            // Receive length of a file name
            if (curState == State.DEL_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.DEL_NAME;
                    System.out.println("Deleted file name length received");
                }
            }
            // Receive file name
            if (curState == State.DEL_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    fileName = new String(nameBytes, StandardCharsets.UTF_8);
                    curState = State.IDLE;
                }
                CmdService.deleteFile(fileName);
                System.out.println("File deleted");
                //send storage content to user
                f = CmdService.getFolderContent(userPath, login);
                if (f == null){
                    ctx.writeAndFlush(MsgLib.MsgType.EMP);
                } else {
                    ctx.writeAndFlush(f);
                }
            }




            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
