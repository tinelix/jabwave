package dev.tinelix.jabwave.api.base.enums;

public class HandlerMessages {
    // Authorization
    public static final int AUTHORIZED                        =   1;
    public static final int DONE                              =   2;

    // Account
    public static final int ACCOUNT_LOADED                    =   100;

    // Chats
    public static final int CHATS_LOADED                      =   200;
    public static final int CHATS_UPDATED                     =   201;
    public static final int FILES_UPDATED                     =   202;

    // Messenger
    public static final int MESSAGE_SENT                      =   300;

    // Errors
    public static final int NO_INTERNET_CONNECTION            =   -1;
    public static final int CONNECTION_TIMEOUT                =   -2;
    public static final int UNKNOWN_ERROR                     =   -3;
    public static final int REQUIRED_AUTH_CODE                =   -4;
    public static final int REQUIRED_CLOUD_PASSWORD           =   -5;
    public static final int AUTHENTICATION_ERROR              =   -6;
}
