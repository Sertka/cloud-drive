package ru.stk.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.scene.Scene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.common.MsgLib;
import ru.stk.common.Settings;
import ru.stk.gui.LoginFxCtl;
import ru.stk.gui.MainFxCtl;
import ru.stk.gui.MsgBox;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Class performs protocol communications with server
 */
public class ClientInHandler extends ChannelInboundHandlerAdapter {

    //define current stage in file receiving from server
    public enum State {
        IDLE, NAME_LEN, NAME, FILE_LEN, FILE
    }

    private State curState = State.IDLE;
    private int fileNameLength;
    private long fileLength;
    private long inFileLength;
    private BufferedOutputStream fileSaveStream;
    private final byte[] msgBytes = new byte[MsgLib.MSG_LEN];
    private String msgString = "";
    private String fileName;
    private final LoginFxCtl lfx; //controller for login form
    private final MainFxCtl mfx; //controller for main form
    private Scene mainScene;

    private static final Logger logger = LogManager.getLogger(Client.class);

    public ClientInHandler (LoginFxCtl lfx, MainFxCtl mfx){
        this.lfx = lfx;
        this.mfx = mfx;
    }

    /*
     * Processes messages from server
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {

            // receive message
            if (curState == State.IDLE) {
                buf.readBytes(msgBytes);
                msgString = new String(msgBytes, StandardCharsets.UTF_8);
            }

            // command of the empty user directory received - EMP
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.EMP.toString())) {
                mfx.fillFileTable(null);
                logger.info("Empty user folder received");
            }

            // user authorisation successful - ATS
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.ATS.toString())) {
                logger.info("Login successful");
                lfx.loginSuccessful();
            }

            // user authorisation failed - ATF
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.ATF.toString())) {
                logger.info("Login failed");
                lfx.loginFailed("Неверный логин или пароль!");
            }

            // user authorisation failed - ATF
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.FER.toString())) {
                logger.info("Error sever folder creation");
                lfx.loginFailed("Ошибка создания раздела на сервере");
            }

            // file successfully downloaded - DLS
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.DLS.toString())) {
                mfx.fileDownloaded();
            }

            // file rename error - RNE
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.RNE.toString())) {
                logger.info("Error file renaming");
                mfx.renameFailed();
            }

            // file delete error - DLE
            if (curState == State.IDLE & msgString.equals(MsgLib.MsgType.DLE.toString())) {
                logger.info("Error file deleting");
                mfx.deleteFailed();
            }

            // command of the incoming new file received - FLE
            if (curState == State.IDLE & msgString.equals("FLE")) {
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
                    byte[] fileByte = new byte[fileNameLength];
                    buf.readBytes(fileByte);

                    fileName = new String(fileByte, StandardCharsets.UTF_8);
                    fileSaveStream = new BufferedOutputStream
                            (new FileOutputStream(Settings.C_FOLDER + "/" +fileName));
                    curState = State.FILE_LEN;
                    if (!(fileName.equals(Client.getLogin() + ".tmp"))) {
                    logger.info("Start file receiving - " + fileName);}
                }
            }

            // receive length of a new file
            if (curState == State.FILE_LEN) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    curState = State.FILE;
                }
            }

            //receive and save content of the file
            if (curState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    fileSaveStream.write(buf.readByte());
                    inFileLength++;
                    if (fileLength == inFileLength) {
                        curState = State.IDLE;

                        fileSaveStream.close();
                        //if temporary file with user file list received - update file list on main form
                        if (fileName.equals(Client.getLogin() + ".tmp")) {
                            mfx.fillFileTable(Client.prepareFileList());
                        } else {
                            logger.info("File is successfully received and saved - " + fileName);
                            }
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    /*
     * Processes Netty exceptions
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        logger.error(cause.getMessage());
        Platform.runLater(() ->MsgBox.showErrorMsg(mainScene, cause.getMessage()));
    }
}