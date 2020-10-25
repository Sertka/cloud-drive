package ru.stk.server;

import ru.stk.common.MsgLib;
import ru.stk.common.Settings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class CmdService {

public static String userLogin;

    public static String clientLogin(String login, String pass, AuthController authCtl) {

        boolean authRes = authCtl.checkUser(login, pass);
        if (authRes) {
            String path = Settings.S_FOLDER + "/" + login + "/";
            File folder = new File(path);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    /*todo: make an exception */
                    System.out.println("Not possible to create a client folder");
                    return "FOLDER_ERR";
                }
            } else {
                userLogin = login;
                return path;
            }
        }
        return "AUTH_FAILED";
    }

    /**
     * Checks content of user's folder and writes list of files in "login".tmp file on the server
     * @param userFolder
     * @return
     */
    public static File getFolderContent (String userFolder, String login) throws IOException {
        String fileSize;

        Writer fileWriter;
        File dir = new File(userFolder);
        File tmp = new File (login + ".tmp");

        if (!dir.isDirectory() || Objects.requireNonNull(dir.listFiles()).length == 0){
            System.out.println("Not possible to find user folder on server");
            return null;
        }


        fileWriter = new FileWriter(tmp);
        for (File item : Objects.requireNonNull(dir.listFiles())) {
            if (item.isFile()) {
                //Calculate file size and convert to String
                if (item.length()/(1024*1024) >= 1L){
                    fileSize = Long.toString(item.length()/(1024*1024)) + " Mb";
                } else{
                    fileSize = Long.toString(item.length()/(1024)) + " Kb";
                }
                SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");

                String strFile = item.getName() + MsgLib.DELIMITER + fileSize + MsgLib.DELIMITER
                        + formatDate.format(item.lastModified()) + MsgLib.DELIMITER + item.length()/(1024) ;

                fileWriter.write(strFile + System.getProperty("line.separator"));
            }
        }
        fileWriter.close();
        return tmp;
    }

    public static boolean renameFile (String oldName, String newName, String login){
        File oldFile = Paths.get(Settings.S_FOLDER + "/" + login + "/" + oldName).toFile();
        File newFile = Paths.get(Settings.S_FOLDER + "/" + login + "/" + newName).toFile();
        return  oldFile.renameTo(newFile);
    }

    public static boolean deleteFile (String delName){
        File delFile = Paths.get(Settings.S_FOLDER + "/" + userLogin + "/" + delName).toFile();
        return  delFile.delete();
    }
}
