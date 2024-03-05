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

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.xmpp.api.entities.Authenticator;
import dev.tinelix.jabwave.net.xmpp.api.models.Roster;

public class XMPPClient extends BaseClient {
    private AbstractXMPPConnection conn;
    private final ApiHandler handler;
    private ConnectionListener connListener;
    public String jid;
    public BareJid bareJid;

    public XMPPClient(AbstractXMPPConnection conn, ApiHandler handler) {
        super(false, "xmpp");
        this.conn = conn;
        this.handler = handler;
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
            handler.onFail(e);
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
        return new Roster(this);
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
