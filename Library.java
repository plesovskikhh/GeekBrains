package ru.geekbrains.java_two.chat.common;

public class Library {
    /*
    * /auth_request±login±password
    * /auth_accept±nickname
    *  /auth_denied
    *  /broadcast±time±src±msg
    *  /msg_format_error
    *  /user_list±user1±user2±user3,....
    *  /client_bcast±msg
    *  /client_private±recipient±msg
    *  /client_snooze
    *  /auth_timeOut
     */

    public static final String DELIMITER = "±";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/auth_denied";
    public static final String MSG_FORMAT_ERROR = "/msg_format_error";
    // если не поняли что за сообщение и не смогли его разобрать
    public static final String TYPE_BROADCAST = "/bcast";
    // то есть сообщение которое будет посылаться всем
    public static final String USER_LIST = "/user_list";
    public static final String TYPE_BCAST_CLIENT = "/client_bcast";
    public static final String AUTH_TIMEOUT = "/auth_timeOut";

    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return AUTH_DENIED;
    }

    public static String getMsgFormatError(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeBroadcast(String src, String message) {
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

    public static String getUserList(String users) {
        return USER_LIST + DELIMITER + users;
    }

    public static String getTypeClientBcast(String msg) {
        return TYPE_BCAST_CLIENT + DELIMITER + msg;
    }

    public static  String getAuthTimeout() {
        return AUTH_TIMEOUT;
    }

}
