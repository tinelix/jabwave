package dev.tinelix.jabwave;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import org.jxmpp.jid.parts.Resourcepart;

import java.util.Random;

import dev.tinelix.jabwave.core.activities.MainActivity;
import dev.tinelix.jabwave.core.services.base.ClientService;

public class JabwaveApp extends Application {
    public String version;
    public ClientService clientService;
    private SharedPreferences global_prefs;
    private SharedPreferences xmpp_prefs;
    private SharedPreferences telegram_prefs;
    public static final String XMPP_SERV_TAG = "XMPPService";
    public static final String TELEGRAM_SERV_TAG = "TDLibService";
    public static final String APP_TAG = "Jabwave";

    @Override
    public void onCreate() {
        super.onCreate();
        version = BuildConfig.VERSION_NAME;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        xmpp_prefs = getSharedPreferences("xmpp", 0);
        telegram_prefs = getSharedPreferences("telegram", 0);
        createSettings();
    }

    private void createSettings() {
        // Application preferences
        SharedPreferences.Editor editor = global_prefs.edit();
        if(!global_prefs.contains("network_type")) {
            editor.putString("network_type", "");
        }
        editor.apply();

        // XMPP client preferences
        editor = xmpp_prefs.edit();
        if(!xmpp_prefs.contains("server")) {
            editor.putString("server", "");
        }
        if(!xmpp_prefs.contains("username")) {
            editor.putString("username", "");
        }
        if(!xmpp_prefs.contains("password_hash")) {
            editor.putString("password_hash", "");
        }
        if(!xmpp_prefs.contains("account_password_sha256")) {
            editor.putString("account_password_sha256", "");
        }

        editor.apply();

        // Telegram API preferences
        editor = telegram_prefs.edit();
        if(!telegram_prefs.contains("phone_number")) {
            editor.putString("phone_number", "");
        }
        if(!telegram_prefs.contains("account_password_hash")) {
            editor.putString("account_password_hash", "");
        }
        if(!global_prefs.contains("enable_notification")) {
            editor.putBoolean("enable_notification", true);
        }
        if(!global_prefs.contains("use_ssl_connection")) {
            editor.putBoolean("use_ssl_connection", true);
        }
        editor.apply();
    }

    public boolean isAuthorized() {
        return getXmppPreferences().getString("server", "").length() > 0 ||
                getTelegramPreferences().getString("phone_number", "").length() > 0;
    }

    public SharedPreferences getGlobalPreferences() {
        return global_prefs;
    }

    public SharedPreferences getXmppPreferences() {
        return xmpp_prefs;
    }

    public SharedPreferences getTelegramPreferences() {
        return telegram_prefs;
    }

    public String getCurrentNetworkType() {
        return global_prefs.getString("network_type", "");
    }

    public void restart() {
        Intent activity = new Intent(this,
                MainActivity.class);
        activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activity);
        System.exit(0);
    }
}
