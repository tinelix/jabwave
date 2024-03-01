package dev.tinelix.jabwave;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import dev.tinelix.jabwave.telegram.services.TelegramService;

public class JabwaveApp extends Application {
    public String version;
    private SharedPreferences global_prefs;
    private SharedPreferences telegram_prefs;
    public TelegramService telegram;
    public static final String TELEGRAM_SERV_TAG = "TGService";
    public static final String APP_TAG = "Jabwave";

    @Override
    public void onCreate() {
        super.onCreate();
        telegram = new TelegramService();
        version = BuildConfig.VERSION_NAME;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        telegram_prefs = getSharedPreferences("telegram", 0);
        if(!telegram_prefs.contains("server")) {
            SharedPreferences.Editor editor = telegram_prefs.edit();
            editor.putString("server", "");
            editor.apply();
        }
        if(!telegram_prefs.contains("username")) {
            SharedPreferences.Editor editor = telegram_prefs.edit();
            editor.putString("username", "");
            editor.apply();
        }
        if(!telegram_prefs.contains("account_password_sha256")) {
            SharedPreferences.Editor editor = telegram_prefs.edit();
            editor.putString("account_password_sha256", "");
            editor.apply();
        }
        if(!telegram_prefs.contains("enable_notification")) {
            SharedPreferences.Editor editor = global_prefs.edit();
            editor.putBoolean("enable_notification", true);
            editor.apply();
        }
    }

    public SharedPreferences getGlobalPreferences() {
        return global_prefs;
    }

    public SharedPreferences getTelegramPreferences() {
        return telegram_prefs;
    }

    @Override
    public void onTerminate() {
        if(telegram.isConnected()) {
            telegram.stopService();
        }
        super.onTerminate();
    }
}
