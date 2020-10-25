package ru.stk.common;

public class MsgLib {

    public static final int MSG_LEN = 3;

    public enum MsgType {
        ATH, //Authorise of user
        FLE, //Send user file
        DNL, // Download a file
        REN, // Rename file
        DEL, // Delete file
        EMP, // Client storage is empty
        ERR  // Err in the requested operation
    }

    public static final String DELIMITER = "¬";

}