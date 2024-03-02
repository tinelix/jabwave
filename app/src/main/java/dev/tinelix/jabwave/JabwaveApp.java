package dev.tinelix.jabwave;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jxmpp.jid.parts.Resourcepart;

import java.util.Random;

import dev.tinelix.jabwave.core.services.TelegramService;
import dev.tinelix.jabwave.xmpp.api.entities.Authentication;
import dev.tinelix.jabwave.xmpp.services.XMPPService;

public class JabwaveApp extends Application {
    public String version;
    private SharedPreferences global_prefs;
    private SharedPreferences xmpp_prefs;
    private SharedPreferences telegram_prefs;
    public XMPPService xmpp;
    public TelegramService telegram;
    public static final String XMPP_SERV_TAG = "XMPPService";
    public static final String TELEGRAM_SERV_TAG = "TDLibService";
    public static final String APP_TAG = "Jabwave";

    @Override
    public void onCreate() {
        super.onCreate();
        xmpp = new XMPPService();
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
        if(!xmpp_prefs.contains("account_password_hash")) {
            editor.putString("account_password_hash", "");
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
        return getXmppPreferences().getString("server", "").length() > 0 &&
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

    public Resourcepart getXMPPResource() {
        try {
            if (getXmppPreferences().contains("jid_resource")) {
                return Resourcepart.from(
                        getXmppPreferences().getString("jid_resource", "")
                );
            } else {
                byte[] random_resource_binary = new byte[] {
                        (byte) new Random().nextInt(255),
                        (byte) new Random().nextInt(255),
                        (byte) new Random().nextInt(255),
                        (byte) new Random().nextInt(255),
                };
                String hex4 = Global.bytesToHex(random_resource_binary);
                String res_name = String.format("TinelixJabwave-%s", hex4);
                Resourcepart res_part = Authentication.generateXMPPResource(res_name);
                SharedPreferences.Editor editor = getXmppPreferences().edit();
                editor.putString("jid_resource", res_name);
                editor.apply();
                return res_part;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void onTerminate() {
        if(xmpp.isConnected()) {
            xmpp.stopService();
        }
        super.onTerminate();
    }
}
