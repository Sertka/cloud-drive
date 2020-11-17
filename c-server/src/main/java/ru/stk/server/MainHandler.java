package ru.stk.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.common.MsgLib;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Class Handler uses Netty functionality to establish connections with clients and
 * handle commands from clients
 */
public class MainHandler extends ChannelInboundHandlerAdapter {
    private final AuthController authCtl;
    
    public MainHandler (AuthController autCtl){
        this. authCtl = autCtl;
    }

    //Defines current stage in communication with client
    private enum State {
        IDLE,
        NAME_LEN, NAME, FILE_LEN, FILE,
        DNL_LEN, DNL_NAME,
        LOGIN_LEN, LOGIN, PASS_LEN, PASS,
        OLD_NAME_LEN, OLD_NAME, NEW_NAME_LEN, NEW_NAME,
        DEL_LEN, DEL_NAME;
    }

    private State curState = State.IDLE;
    private int fileNameLength;     // length of the file name string
    private long fileLength;        // length of the file itself
    private String fileName;        // file name
    private long inFileLength;      // length of the received file part

    private int loginLength;        // login length
    private int passLength;         // password length
    private String login;           // user login
    private String pass;            // user password
    private String userPath;        // path to user folder on server

    private String oldFileName;     // file name before rename
    private String newFileName;     // file name after rename

    private BufferedOutputStream fileSaveStream;
    private final byte[] msgBytes = new byte[MsgLib.MSG_LEN];
    private String msgString = "";

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    /*
     * Netty channel read method
     */
    @Override
    public void channelRead (ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf buf = ((ByteBuf) msg);  // Netty byte buffer
        File f;                         // file for sending to user

        while (buf.readableBytes() > 0) {

            if (curState == State.IDLE) {
                buf.readBytes(msgBytes);
                msgString = new String(msgBytes, StandardCharsets.UTF_8);
            }

            /*
             * This block is responsible for user authorisation
             */

            // receive command of the incoming login and password - ATH
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.ATH.toString())) {
                curState = State.LOGIN_LEN;
            }

            // receive length of a login
            if (curState == State.LOGIN_LEN) {
                if (buf.readableBytes() >= 4) {
                    loginLength = buf.readInt();
                    curState = State.LOGIN;
                }
            }

            // receive login
            if (curState == State.LOGIN) {
                if (buf.readableBytes() >= loginLength) {
                    byte[] loginBytes = new byte[loginLength];
                    buf.readBytes(loginBytes);
                    login = new String(loginBytes, StandardCharsets.UTF_8);
                    curState = State.PASS_LEN;
                }
            }

            // receive password length
            if (curState == State.PASS_LEN) {
                if (buf.readableBytes() >= 4) {
                    passLength = buf.readInt();
                    curState = State.PASS;
                }
            }
            // receive password
            if (curState == State.PASS) {
                if (buf.readableBytes() >= passLength) {
                    byte[] passBytes = new byte[passLength];
                    buf.readBytes(passBytes);
                    pass = new String(passBytes, StandardCharsets.UTF_8);
                    curState = State.IDLE;
                }

                // check user credentials and save user folder
                String authResult = CmdService.clientLogin(login, pass, authCtl);
                if (authResult.equals(MsgLib.MsgType.ATF.toString())) {
                    logger.info("User authorisation failed. Login - " + login);
                    // send "auth. failed" message to user
                    ctx.writeAndFlush(MsgLib.MsgType.ATF);
                } else if(authResult.equals(MsgLib.MsgType.FER.toString())){
                    logger.error("Not possible to create a client folder for user - " + login);
                    // send "folder error" message to user
                    ctx.writeAndFlush(MsgLib.MsgType.FER);
                } else{
                    userPath = authResult;
                    curState = State.IDLE;
                    logger.info("User authorisation successful. Login - " + login);
                    // send "auth. successful" message
                    ctx.writeAndFlush(MsgLib.MsgType.ATS);
                    // send storage content
                    f = CmdService.getFolderContent(userPath, login);
                    if (f == null){
                        ctx.writeAndFlush(MsgLib.MsgType.EMP);
                    } else {
                        ctx.writeAndFlush(f);
                    }
                }
            }

            /*
             * This block is responsible for file receiving
             */

            // receive command of the incoming new file - FLE
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.FLE.toString())) {
                curState = State.NAME_LEN;
                inFileLength = 0L;
            }

            // receive length of a new file name
            if (curState == State.NAME_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.NAME;
                }
            }

            // receive file name and create a new file in user's directory
            if (curState == State.NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    fileName = new String(nameBytes, StandardCharsets.UTF_8);
                    fileSaveStream = new BufferedOutputStream
                            (new FileOutputStream(userPath + fileName));
                    curState = State.FILE_LEN;
                }
            }

            // receive length of a new file
            if (curState == State.FILE_LEN) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    curState = State.FILE;
                }
            }

            // receive and save content of the file
            if (curState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    fileSaveStream.write(buf.readByte());
                    inFileLength++;
                    if (fileLength == inFileLength) {
                        curState = State.IDLE;
                        fileSaveStream.close();
                        logger.info("File received - " + fileName);

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

            /*
             * This block is responsible for file sending
             */

            // receive command of the file download request - DNL
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.DNL.toString())) {
                curState = State.DNL_LEN;
            }

            // receive length of a file name
            if (curState == State.DNL_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.DNL_NAME;
                }
            }

            // receive file name
            if (curState == State.DNL_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    fileName = new String(nameBytes, StandardCharsets.UTF_8);
                    curState = State.IDLE;
                }
                // send file to user
                f = Paths.get(userPath + fileName).toFile();
                ctx.writeAndFlush(f);
                logger.info("File downloaded - " + fileName);
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

            // receive command of file renaming- REN
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.REN.toString())) {
                curState = State.OLD_NAME_LEN;
            }

            // receive length of an old file name
            if (curState == State.OLD_NAME_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.OLD_NAME;
                }
            }

            // receive old name
            if (curState == State.OLD_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    oldFileName = new String(nameBytes, StandardCharsets.UTF_8);
                    curState = State.NEW_NAME_LEN;
                }
            }

            // receive length of a new file name
            if (curState == State.NEW_NAME_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.NEW_NAME;
                }
            }
            // receive new name
            if (curState == State.NEW_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    newFileName = new String(nameBytes, StandardCharsets.UTF_8);
                    curState = State.IDLE;
                }
                //rename file
                boolean renameResult = CmdService.renameFile(oldFileName, newFileName, login);
                if (renameResult) {
                    logger.info("File renamed from " + oldFileName + " to " + newFileName);
                    //send storage content to user
                    f = CmdService.getFolderContent(userPath, login);
                    if (f == null){
                        ctx.writeAndFlush(MsgLib.MsgType.EMP);
                    } else {
                        ctx.writeAndFlush(f);
                    }
                } else {
                    logger.info("Not possible to rename the file - " + oldFileName);
                    ctx.writeAndFlush(MsgLib.MsgType.RNE);
                }
            }

            /*
             * This block is responsible for the deleting of a file
             */
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.DEL.toString())) {
                curState = State.DEL_LEN;
            }
            // receive length of a file name
            if (curState == State.DEL_LEN) {
                if (buf.readableBytes() >= 4) {
                    fileNameLength = buf.readInt();
                    curState = State.DEL_NAME;
                    System.out.println("Deleted file name length received");
                }
            }
            // receive file name
            if (curState == State.DEL_NAME) {
                if (buf.readableBytes() >= fileNameLength) {
                    byte[] nameBytes = new byte[fileNameLength];
                    buf.readBytes(nameBytes);
                    fileName = new String(nameBytes, StandardCharsets.UTF_8);
                    curState = State.IDLE;
                }
                boolean deleteResult = CmdService.deleteFile(fileName);

                if (deleteResult) {
                    //send storage content to user
                    f = CmdService.getFolderContent(userPath, login);
                    if (f == null) {
                        ctx.writeAndFlush(MsgLib.MsgType.EMP);
                    } else {
                        ctx.writeAndFlush(f);
                    }
                } else {
                    logger.info("Not possible to delete the file - " + fileName);
                    ctx.writeAndFlush(MsgLib.MsgType.DLE);
                }
            }

            //releasing buffer
            if (buf.readableBytes() == 0) {
                buf.release();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Netty error - " + cause.getMessage());
        ctx.close();
    }
}
