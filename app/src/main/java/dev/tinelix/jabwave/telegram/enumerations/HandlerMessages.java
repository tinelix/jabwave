package dev.tinelix.jabwave.telegram.enumerations;

public class HandlerMessages {
    // Authorization
    public static int AUTHORIZED                        =   1;
    public static int DONE                              =   2;

    // Roster
    public static final int ROSTER_CHANGED              = 100;

    // Errors
    public static int NO_INTERNET_CONNECTION            =  -1;
    public static int CONNECTION_TIMEOUT                =  -2;
    public static int UNKNOWN_ERROR                     =  -3;
    public static int REQUIRED_AUTH_CODE                =  -4;
    public static int REQUIRED_CLOUD_PASSWORD           =  -5;
    public static int AUTHENTICATION_ERROR              =  -6;
}
