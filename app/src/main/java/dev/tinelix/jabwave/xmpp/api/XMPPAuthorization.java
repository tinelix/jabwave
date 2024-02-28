package dev.tinelix.jabwave.xmpp.api;

import android.annotation.SuppressLint;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.SslContextFactory;
import org.jivesoftware.smack.util.TLSUtils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class XMPPAuthorization {
    public static XMPPTCPConnectionConfiguration buildConnectionConfig(
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
}
