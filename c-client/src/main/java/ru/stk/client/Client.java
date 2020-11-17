package ru.stk.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.common.FileSender;
import ru.stk.common.MsgLib;
import ru.stk.common.Settings;
import ru.stk.gui.LoginFxCtl;
import ru.stk.gui.MainFxCtl;
import ru.stk.gui.MsgBox;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

/**
 * Class "client" contains static methods with business logic on client side
 */
public class Client {
    private static String userLogin = "";   //user login is used to generate path to files
    private static String userFolderSize;   //total size of files in user folder
    private static int userFolderCount;     //total amount of use files in use folder
    private static Scene mainScene;         //contains main form

    private static final Logger logger = LogManager.getLogger(Client.class);

    public static String getFolderSize(){
        return userFolderSize;
    }

    public static String getFolderCount(){
        return Integer.toString(userFolderCount);
    }

    public static String getLogin(){
        return userLogin;
    }

    public static void setScene(Scene scene){
        mainScene = scene;
    }

    /*
     * Establishes connection with server. Started on "login" form by button "Connect"
     */


    public static void connect(LoginFxCtl lfx, MainFxCtl mfx){
        CountDownLatch networkStarter = new CountDownLatch(1);


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Network.getInstance().start(networkStarter, lfx, mfx, mainScene);
                } catch (Exception e) {
                    logger.error("Server connection failure - " + e.getMessage());
                    lfx.noServer();
                }
            }
        });
        t.start();
        logger.info("Connection with server started");
        // waiting for connection to avoid early file sending
        try {
            networkStarter.await();
        } catch (InterruptedException e) {
            logger.error("Server connection failure - " + e.getMessage());
        }
    }

    /*
     * Sends auth data to server
     */
    public static void authClient (String login, String pass){
        userLogin = login;
        ByteBuf buf = null;

        // send command login and password - ATH means authorisation data
        byte[] msgBytes = MsgLib.MsgType.ATH.toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(buf);

        //send login
        sendString(login);
        //send password
        sendString(pass);

        logger.info("Authorisation data provided");
    }


    /**
     * Receives file and calls FileSender to send the file to sever
     */
    public static void sendClientFile(Path filePath) throws IOException {
        FileSender.sendFile(filePath, Network.getInstance().getCurrentChannel(), null, new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                    logger.error("Sending of file " + filePath.getFileName() +
                            " is failed, reason - " + future.cause());
                    Platform.runLater(() -> MsgBox.showErrorMsg(mainScene, "Невозможно отправить файл!"));
                }
                if (future.isSuccess()) {
                    logger.info("File " + filePath.getFileName() + " has been successfully sent to server");
                }
            }
        });
    }

    /**
     * Downloads defined file from server and saves on user's PC in defined folder.
     * Folder is currently set up in c-common.Settings
     */
    public static void downloadFile (String fileName){
        ByteBuf buf = null;
        // send command "download file" - DNL
        byte[] msgBytes = MsgLib.MsgType.DNL.toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(buf);
        //send file name for downloading
        sendString(fileName);
    }

    /**
     * Renames the selected file
     */
    public static void renameFile (String fileName) {
        ByteBuf buf = null;
        String newName = "";

        TextInputDialog dialog = new TextInputDialog(fileName);

        dialog.setTitle("cloud.drive");
        dialog.setHeaderText("Переименование файла");
        dialog.setContentText("Новое имя файла:");
        dialog.initOwner(mainScene.getWindow());

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            newName = result.get();

            // Sends command to rename the file - REN
            byte[] msgBytes = MsgLib.MsgType.REN.toString().getBytes(StandardCharsets.UTF_8);
            buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
            buf.writeBytes(msgBytes);
            send(buf);

            //send old file name
            sendString(fileName);
            //send new file name
            sendString(newName);
        }
    }


    /**
     * Deletes the selected file
     */
    public static void deleteFile (String fileName) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("cloud.drive");
        alert.setHeaderText("Удаление файла");
        alert.setContentText("Вы уверены, что хотите удалить файл " + fileName);

        alert.initOwner(mainScene.getWindow());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == ButtonType.OK) {

                ByteBuf buf = null;
                //Sends command to delete the file - DEL
                byte[] msgBytes = MsgLib.MsgType.DEL.toString().getBytes(StandardCharsets.UTF_8);
                buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
                buf.writeBytes(msgBytes);
                send(buf);

                //send file name to delete
                sendString(fileName);
            }
        }
    }


    /**
     * Prepares the collections of objects (files). Is used to fill in the file list on the
     * main form
     */
    public static ObservableList<UserFile> prepareFileList() throws IOException {
        String[] tmp;
        userFolderCount = 0;
        userFolderSize = "";
        long overallSize = 0L;
        ObservableList<UserFile> files = FXCollections.observableArrayList();

        File file  = Paths.get(Settings.C_FOLDER + "/" + userLogin + ".tmp").toFile();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line = reader.readLine();

        // reset the "total" fields if file list is empty
        if (line == null || file.length() == 0) {
            userFolderSize = "";
            userFolderCount = 0;
            return null;
        }
        while (line != null) {
            tmp = line.split(MsgLib.DELIMITER);
            files.add(new UserFile(tmp[0], tmp[1], tmp[2]));
            line = reader.readLine();
            overallSize += Long.parseLong(tmp[3]);
            userFolderCount++;
        }
        //fill in total folder size
        if (overallSize/(1024) >= 1L){
            userFolderSize = Long.toString(overallSize/(1024)) + " Mb";
        } else{
            userFolderSize = Long.toString(overallSize) + " Kb";
        }
        // delete temporary file
        file.delete();

        return files;
    }

    /*
     * Sends incoming string to server
     */
    private static void sendString(String str){
        ByteBuf buf = null;

        // send string length
        byte[] msgBytes = str.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(msgBytes.length);
        send(buf);

        // send string
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(buf);
    }

    /*
     * Sends incoming Netty byte buffer to server
     */
    private static void send(ByteBuf buf){
        Channel channel = Network.getInstance().getCurrentChannel();
        channel.writeAndFlush(buf);
    }
}
