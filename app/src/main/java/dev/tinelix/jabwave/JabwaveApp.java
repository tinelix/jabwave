package dev.tinelix.jabwave;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dev.tinelix.jabwave.xmpp.services.XMPPService;

public class JabwaveApp extends Application {
    public String version;
    private SharedPreferences global_prefs;
    private SharedPreferences xmpp_prefs;
    public XMPPService xmpp;
    public static final String XMPP_SERV_TAG = "XMPPService";
    public static final String APP_TAG = "Jabwave";
    @Override
    public void onCreate() {
        super.onCreate();
        xmpp = new XMPPService();
        version = BuildConfig.VERSION_NAME;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        xmpp_prefs = getSharedPreferences("xmpp", 0);
        if(!xmpp_prefs.contains("server")) {
            SharedPreferences.Editor editor = xmpp_prefs.edit();
            editor.putString("server", "");
            editor.apply();
        }
        if(!xmpp_prefs.contains("username")) {
            SharedPreferences.Editor editor = xmpp_prefs.edit();
            editor.putString("username", "");
            editor.apply();
        }
        if(!xmpp_prefs.contains("account_password_sha256")) {
            SharedPreferences.Editor editor = xmpp_prefs.edit();
            editor.putString("account_password_sha256", "");
            editor.apply();
        }
        if(!global_prefs.contains("enable_notification")) {
            SharedPreferences.Editor editor = global_prefs.edit();
            editor.putBoolean("enable_notification", true);
            editor.apply();
        }
        if(!global_prefs.contains("use_ssl_connection")) {
            SharedPreferences.Editor editor = global_prefs.edit();
            editor.putBoolean("use_ssl_connection", true);
            editor.apply();
        }
    }

    public SharedPreferences getGlobalPreferences() {
        return global_prefs;
    }

    public SharedPreferences getXmppPreferences() {
        return xmpp_prefs;
    }

    @Override
    public void onTerminate() {
        if(xmpp.isConnected()) {
            xmpp.stopService();
        }
        super.onTerminate();
    }
}
