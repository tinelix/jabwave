package dev.tinelix.jabwave;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jxmpp.jid.parts.Resourcepart;

import java.util.Random;

import dev.tinelix.jabwave.xmpp.api.entities.Authentication;
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
