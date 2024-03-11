package dev.tinelix.jabwave.net.xmpp.api;

import android.content.Context;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.HashMap;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.base.listeners.OnClientUpdateListener;
import dev.tinelix.jabwave.net.xmpp.api.entities.Authenticator;
import dev.tinelix.jabwave.net.xmpp.api.stanzas.ClientVersionStanza;

public class XMPPClient extends BaseClient {
    private final Context ctx;
    private AbstractXMPPConnection conn;
    private final OnClientAPIResultListener listener;
    private ConnectionListener connListener;
    public String jid;
    public EntityBareJid entitiyBareJid;
    public String server;

    public XMPPClient(Context ctx, AbstractXMPPConnection conn, OnClientAPIResultListener listener) {
        super(false, "xmpp");
        this.conn = conn;
        this.listener = listener;
        this.ctx = ctx;
    }

    public void start(String server, String jid, String password) {
        this.server = server;
        this.jid = jid;
        SmackConfiguration.DEBUG = true;
        try {
            XMPPTCPConnectionConfiguration config =
                    Authenticator.buildAuthConfig(
                            server,
                            jid,
                            password,
                            true
                    );
            conn = new XMPPTCPConnection(config);
            conn.addStanzaListener(packet -> {
                try {
                    ClientIdentityParams params = new ClientIdentityParams(ctx);
                    HashMap<String, String> map = params.getClientIdentity();
                    ClientVersionStanza stanza = new ClientVersionStanza(packet.getFrom().toString());
                    stanza.setClientInfo(
                            map.get("client_name"),
                            map.get("client_version"),
                            map.get("os_version"));
                    sendStanza(stanza);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, stanza -> stanza.getNamespace().equals("jabber:iq:version"));
            conn.connect();
            conn.login(jid, password, Authenticator.generateXMPPResource());
            Presence presence = conn
                    .getStanzaFactory()
                    .buildPresenceStanza()
                    .setMode(Presence.Mode.available)
                    .ofType(Presence.Type.available)
                    .build();
            sendStanza(presence);
            entitiyBareJid = JidCreate.entityBareFrom(String.format("%s@%s", jid, server));
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
            entitiyBareJid = JidCreate.entityBareFrom(String.format("%s@%s", jid, server));
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFail(new HashMap<>(), e);
        }
    }

    public AbstractXMPPConnection getConnection() {
        return conn;
    }

    @SuppressWarnings("Convert2MethodRef")
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
