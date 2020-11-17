package ru.stk.common;

/**
 * Describes communication rules
 */
public class MsgLib {

    public static final int MSG_LEN = 3;

    public enum MsgType {

        ATH,    // authorise of user
        FLE,    // send user file
        DNL,    // download a file
        REN,    // rename file
        DEL,    // delete file
        EMP,    // client storage is empty
        ATS,    // authorisation successful
        DLS,    // file download successful
        ATF,    // authorisation failed
        FER,    // folder creation error
        RNE,    // file rename error
        DLE     // file delete error
    }

    public static final String DELIMITER = "¬";

}