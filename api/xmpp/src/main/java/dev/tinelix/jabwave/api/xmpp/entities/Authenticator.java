package dev.tinelix.jabwave.api.xmpp.entities;

import android.annotation.SuppressLint;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.SslContextFactory;
import org.jivesoftware.smack.util.TLSUtils;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dev.tinelix.jabwave.api.base.Global;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.xmpp.XMPPClient;

public class Authenticator extends dev.tinelix.jabwave.api.base.entities.Authenticator {

    private XMPPTCPConnection conn;

    public Authenticator(XMPPClient client) {
        super(client);
        conn = (XMPPTCPConnection) client.getConnection();
        isChangeableAuthData = true;
        isChangeableEmail = false;
        isChangeablePhoneNumber = false;
    }

    public static XMPPTCPConnectionConfiguration buildAuthConfig(
            String server,
            String jid,
            String password,
            boolean tls_allowed) {
        try {
            XMPPTCPConnectionConfiguration.Builder configBuilder =
                    XMPPTCPConnectionConfiguration.builder()
                            .setUsernameAndPassword(jid, password)
                            .setXmppDomain(server)
                            .setHost(server)
                            .setPort(5222)
                            .setLanguage(Locale.getDefault())
                            .setSendPresence(false)
                            .setCompressionEnabled(false)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.required);
            if(tls_allowed) {
                TLSUtils.setEnabledTlsProtocolsToRecommended(configBuilder);
                configBuilder = createSecureSocketContext(configBuilder);
            }
            return configBuilder.build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static XMPPTCPConnectionConfiguration.Builder createSecureSocketContext(
            XMPPTCPConnectionConfiguration.Builder configBuilder
    ) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SslContextFactory scf = () -> sc;
            configBuilder.setSslContextFactory(scf);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return configBuilder;
    }

    public static Resourcepart generateXMPPResource() {
        try {
            byte[] random_resource_binary = new byte[] {
                    (byte) new Random().nextInt(255),
                    (byte) new Random().nextInt(255),
                    (byte) new Random().nextInt(255),
                    (byte) new Random().nextInt(255),
            };
            String hex4 = Global.bytesToHex(random_resource_binary);
            return Resourcepart.from(String.format("TinelixJabwave-%s", hex4));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Resourcepart generateXMPPResource(String res_name) {
        try {
            return Resourcepart.from(res_name);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void signIn(Object object) {
        if(object instanceof HashMap<?,?> map) {
            String jid = (String) map.get("jid");
            String password = (String) map.get("password");
            if(jid != null && password != null) {
                try {
                    conn.login(jid, password);
                    isAuthenticated = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    ((XMPPClient) client).start(jid.split("@")[1], jid.split("@")[0], password);
                    isAuthenticated = true;
                }
            }
        }
    }

    @Override
    public void signIn(Object object, OnClientAPIResultListener listener) {
        if(object instanceof HashMap<?,?> map) {
            String jid = (String) map.get("jid");
            String password = (String) map.get("password");
            if(jid != null && password != null) {
                try {
                    conn.login(jid, password);
                    isAuthenticated = true;
                    listener.onSuccess(new HashMap<>());
                } catch (Exception e) {
                    try {
                        ((XMPPClient) client).start(jid.split("@")[1], jid.split("@")[0], password);
                        isAuthenticated = true;
                        listener.onSuccess(new HashMap<>());
                    } catch (Exception e1) {
                        e.printStackTrace();
                        listener.onFail(new HashMap<>(), e1);
                    }
                }
            }
        }
    }
}
