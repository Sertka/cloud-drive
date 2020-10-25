package ru.stk.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import ru.stk.common.FileSender;
import ru.stk.common.MsgLib;
import ru.stk.common.Settings;
import ru.stk.gui.MainFxCtl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Class performs all communications with server
 * @version 1.0 17 Oct 2020
 * @author    Sergei Tkachev
 */
public class Client {
    private static String userLogin = "";
    private static Scene mainScene;
    private static String userFolderSize;
    private static int userFolderCount;

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

    public static void connect(MainFxCtl fxc) throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Network.getInstance().start(networkStarter, fxc);
            }
        }).start();
        /* Waiting for connection to avoid early file sending */
        networkStarter.await();
    }


    public static void authClient (String login, String pass){
        userLogin = login;

        ByteBuf buf = null;

        /* Sends command login and password - ATH means authorisation data */
        byte[] msgBytes = MsgLib.MsgType.ATH.toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(buf);

        /*Send login */
        sendString(login);

        /*Send pass */
        sendString(pass);
    }


    /**
     * Gets file and call FileSender to send the file to sever
     */
    public static void sendClientFile(Path filePath) throws IOException {
        FileSender.sendFile(filePath, Network.getInstance().getCurrentChannel(), null, new ChannelFutureListener() {
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

    public static void downloadFile (String fileName){
        ByteBuf buf = null;
        // send command "download file"
        byte[] msgBytes = MsgLib.MsgType.DNL.toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(buf);
        //send file name for downloading
        sendString(fileName);
    }


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

            // Sends command login and password - ATH means authorisation data
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

    public static void deleteFile (String fileName) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("cloud.drive");
        alert.setHeaderText("Удаление файла");
        alert.setContentText("Вы уверены, что хотите удалить файл" + fileName);

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

                //send old file name
                sendString(fileName);
            }
        }
    }



    /**
     * Sends incoming string to server
     * @param str - incoming string
     */
    private static void sendString(String str){
        ByteBuf buf = null;
        /*Sends string length */
        byte[] msgBytes = str.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(msgBytes.length);
        send(buf);

        /*Sends string */
        buf = ByteBufAllocator.DEFAULT.directBuffer(msgBytes.length);
        buf.writeBytes(msgBytes);
        send(buf);
    }



    public static ObservableList<UserFile> prepareFileList() throws IOException {
        String[] tmp;
        userFolderCount = 0;
        userFolderSize = "";
        long overallSize = 0L;
        ObservableList<UserFile> files = FXCollections.observableArrayList();

        File file  = Paths.get(Settings.C_FOLDER + "/" + userLogin + ".tmp").toFile();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line = reader.readLine();

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
        if (overallSize/(1024) >= 1L){
            userFolderSize = Long.toString(overallSize/(1024)) + " Mb";
        } else{
            userFolderSize = Long.toString(overallSize) + " Kb";
        }

        file.delete();
        return files;
    }

    private static void send(ByteBuf buf){
        Channel channel = Network.getInstance().getCurrentChannel();
        channel.writeAndFlush(buf);
    }



}
