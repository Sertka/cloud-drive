package ru.stk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.common.MsgLib;
import ru.stk.common.Settings;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Objects;


/**
 * Provides static methods to handle user's request to the server
 */
public class CmdService {
    public static String userLogin;

    /*
     * Checks user's authorisation data and returns path to user folder on server
     */
    public static String clientLogin(String login, String pass, AuthController authCtl) {

        boolean authRes = authCtl.checkUser(login, pass);
        if (authRes) {
            String path = Settings.S_FOLDER + "/" + login + "/";
            File folder = new File(path);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    return MsgLib.MsgType.FER.toString();
                }
            } else {
                userLogin = login;
                return path;
            }
        }
        return MsgLib.MsgType.ATF.toString();
    }

    /*
     * Checks content of user's folder and writes list of files in "login".tmp file on the server
     */
    public static File getFolderContent (String userFolder, String login) throws IOException {
        String fileSize;

        Writer fileWriter;
        File dir = new File(userFolder);
        File tmp = new File (login + ".tmp");

        // if user folder not found ot empty
        if (!dir.isDirectory() || Objects.requireNonNull(dir.listFiles()).length == 0){
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
                // format for the file "last modified" date and time
                SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");

                String strFile = item.getName() + MsgLib.DELIMITER + fileSize + MsgLib.DELIMITER
                        + formatDate.format(item.lastModified()) + MsgLib.DELIMITER + item.length()/(1024) ;

                fileWriter.write(strFile + System.getProperty("line.separator"));
            }
        }
        fileWriter.close();
        return tmp;
    }

    /*
     * Renames file upon the user's request
     */
    public static boolean renameFile (String oldName, String newName, String login){
        File oldFile = Paths.get(Settings.S_FOLDER + "/" + login + "/" + oldName).toFile();
        File newFile = Paths.get(Settings.S_FOLDER + "/" + login + "/" + newName).toFile();
        return  oldFile.renameTo(newFile);
    }

    /*
     * Deletes file upon the user's request
     */
    public static boolean deleteFile (String delName){
        File delFile = Paths.get(Settings.S_FOLDER + "/" + userLogin + "/" + delName).toFile();
        return delFile.delete();
    }
}
