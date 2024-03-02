package dev.tinelix.jabwave.telegram;

import com.mediaparkpk.base58android.Base58;
import com.mediaparkpk.base58android.Base58Exception;

import dev.tinelix.jabwave.BuildConfig;

public class APISecureStorage {
    /**
        <h2>Secure storage for Telegram API Keys</h2>
        See <i><code>getTDAppToken()</code></i> function in <code>build.gradle</code>
        project file for changing API keys.
        <br>
        <br>
        Application Token & ID can be obtained from
        <a href="my.telegram.org">Telegram Developers website</a>.
    **/
    public static String app_id = "";
    public static String app_key = "";

    public static void loadAppToken() {
        try {
            app_id = new String(Base58.decode(BuildConfig.TDLIB_APP_TOKEN)).split("\\.")[0];
            app_key = new String(Base58.decode(BuildConfig.TDLIB_APP_TOKEN)).split("\\.")[1];
        } catch (Base58Exception e) {
            e.printStackTrace();
        }
    }

}
