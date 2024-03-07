package dev.tinelix.jabwave.net.xmpp.api;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.HashMap;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientUpdateListener;
import dev.tinelix.jabwave.net.xmpp.api.entities.Authenticator;
import dev.tinelix.jabwave.net.xmpp.api.models.Roster;

public class XMPPClient extends BaseClient {
    private AbstractXMPPConnection conn;
    private final OnClientAPIResultListener listener;
    private ConnectionListener connListener;
    public String jid;
    public BareJid bareJid;

    public XMPPClient(AbstractXMPPConnection conn, OnClientAPIResultListener listener) {
        super(false, "xmpp");
        this.conn = conn;
        this.listener = listener;
    }

    public void start(String server, String jid, String password) {
        this.jid = jid;
        try {
            XMPPTCPConnectionConfiguration config =
                    Authenticator.buildAuthConfig(
                            server,
                            jid,
                            password,
                            true
                    );
            conn = new XMPPTCPConnection(config);
            conn.connect();
            conn.login(jid, password, Authenticator.generateXMPPResource());
            Presence presence = conn
                    .getStanzaFactory()
                    .buildPresenceStanza()
                    .setMode(Presence.Mode.available)
                    .ofType(Presence.Type.available)
                    .build();
            sendStanza(presence);
            bareJid = JidCreate.from(String.format("%s@%s", jid, server)).asEntityBareJidOrThrow();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFail(new HashMap<>(), e);
        }
    }

    public void start(String server, String jid, String password, String res_name) {
        try {
            XMPPTCPConnectionConfiguration config =
                    Authenticator.buildAuthConfig(
                            server,
                            jid,
                            password,
                            true
                    );
            conn = new XMPPTCPConnection(config);
            conn.login(jid, password, Authenticator.generateXMPPResource(res_name));
            Presence presence = conn
                    .getStanzaFactory()
                    .buildPresenceStanza()
                    .setMode(Presence.Mode.available)
                    .ofType(Presence.Type.available)
                    .build();
            sendStanza(presence);
            bareJid = JidCreate.from(String.format("%s@%s", jid, server)).asEntityBareJidOrThrow();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFail(new HashMap<>(), e);
        }
    }

    public AbstractXMPPConnection getConnection() {
        return conn;
    }

    public void listenConnection(OnClientAPIResultListener listener) {
        if(connListener != null) {
            conn.removeConnectionListener(connListener);
        }
        connListener = new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                ConnectionListener.super.connected(connection);
                HashMap<String, Object> map = new HashMap<>();
                map.put("connection", conn);
                listener.onSuccess(map);
            }

            @Override
            public void connecting(XMPPConnection connection) {
                ConnectionListener.super.connecting(connection);
            }

            @Override
            public void connectionClosed() {
                ConnectionListener.super.connectionClosed();
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                ConnectionListener.super.connectionClosedOnError(e);
                e.printStackTrace();
                listener.onFail(new HashMap<>(), e);
            }
        };
        conn.addConnectionListener(connListener);
    }

    public void sendStanza(Stanza stanza) {
        try {
            conn.sendStanza(stanza);
            HashMap<String, Object> map = new HashMap<>();
            map.put("connection", conn);
            map.put("function", stanza);
            listener.onSuccess(map);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
            listener.onFail(new HashMap<>(), e);
        }
    }
}
