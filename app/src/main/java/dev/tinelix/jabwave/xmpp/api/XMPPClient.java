package dev.tinelix.jabwave.xmpp.api;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.xmpp.api.entities.Authentication;
import dev.tinelix.jabwave.xmpp.api.entities.Roster;

public class XMPPClient {
    private AbstractXMPPConnection conn;
    private final ApiHandler handler;
    private ConnectionListener connListener;

    public XMPPClient(AbstractXMPPConnection conn, ApiHandler handler) {
        this.conn = conn;
        this.handler = handler;
    }

    public void start(String server, String jid, String password) {
        try {
            XMPPTCPConnectionConfiguration config =
                    Authentication.buildAuthConfig(
                            server,
                            jid,
                            password,
                            true
                    );
            conn = new XMPPTCPConnection(config);
            conn.connect();
            conn.login(jid, password, Authentication.generateXMPPResource());
            Presence presence = conn
                    .getStanzaFactory()
                    .buildPresenceStanza()
                    .setMode(Presence.Mode.available)
                    .ofType(Presence.Type.available)
                    .build();
            sendStanza(presence);
        } catch (Exception e) {
            e.printStackTrace();
            handler.onFail(e);
        }
    }

    public void start(String server, String jid, String password, String res_name) {
        try {
            XMPPTCPConnectionConfiguration config =
                    Authentication.buildAuthConfig(
                            server,
                            jid,
                            password,
                            true
                    );
            conn = new XMPPTCPConnection(config);
            conn.login(jid, password, Authentication.generateXMPPResource(res_name));
            Presence presence = conn
                    .getStanzaFactory()
                    .buildPresenceStanza()
                    .setMode(Presence.Mode.available)
                    .ofType(Presence.Type.available)
                    .build();
            sendStanza(presence);
        } catch (Exception e) {
            e.printStackTrace();
            handler.onFail(e);
        }
    }

    public AbstractXMPPConnection getConnection() {
        return conn;
    }

    public void listenConnection(ApiConnectionHandler handler) {
        if(connListener != null) {
            conn.removeConnectionListener(connListener);
        }
        connListener = new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                ConnectionListener.super.connected(connection);
                handler.onSuccess(conn);
            }

            @Override
            public void connecting(XMPPConnection connection) {
                ConnectionListener.super.connecting(connection);
            }

            @Override
            public void connectionClosed() {
                ConnectionListener.super.connectionClosed();
                handler.onDisconnect();
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                ConnectionListener.super.connectionClosedOnError(e);
                e.printStackTrace();
                handler.onFail(e);
            }
        };
        conn.addConnectionListener(connListener);
    }

    public void sendStanza(Stanza stanza) {
        try {
            conn.sendStanza(stanza);
            handler.onSuccess(conn, stanza);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
            handler.onFail(e);
        }
    }

    public Roster getRoster() {
        return new Roster(conn);
    }

    public interface ApiHandler {
        void onSuccess(XMPPConnection conn, Object object);
        void onFail(Throwable t);
    }

    public interface ApiConnectionHandler {
        void onSuccess(XMPPConnection conn);
        void onFail(Throwable t);
        void onDisconnect();
    }
}