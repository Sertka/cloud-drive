package ru.stk.server;

import ru.stk.common.MsgLib;
import ru.stk.common.Settings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class CmdService {



    public static String clientLogin(String login, String pass) {
        if (login.equals("Serge") & pass.equals("Serge7")) {
            String path = Settings.S_FOLDER + "/" + login;
            File folder = new File(path);
            if (!folder.exists()) {
                if (!folder.mkdir()) {
                    /*todo: make an exception */
                    System.out.println("Not possible to create a client folder");

                }
            } else {
                return path + "/";
            }
        }
        return "";
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

        if (!dir.isDirectory()){
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
                        + formatDate.format(item.lastModified()) ;
                System.out.println(strFile);

                fileWriter.write(strFile + System.getProperty("line.separator"));
            }
        }
        fileWriter.close();
        return tmp;

    }


}
