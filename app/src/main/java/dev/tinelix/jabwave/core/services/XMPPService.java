package dev.tinelix.jabwave.core.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;

import com.mediaparkpk.base58android.Base58;
import com.mediaparkpk.base58android.Base58Exception;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.util.ArrayList;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.xmpp.api.XMPPClient;
import dev.tinelix.jabwave.xmpp.api.entities.Authentication;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.xmpp.api.entities.Roster;
import dev.tinelix.jabwave.xmpp.enumerations.HandlerMessages;

/**
 * XMPP (Smack) client service
 */

public class XMPPService extends IntentService {

    private static final String ACTION_START = "start_service";
    private static final String ACTION_STOP = "stop_service";

    private static final String SERVER = "server";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private Context ctx;

    private String status = "done";

    private XMPPClient client;
    private Intent intent;
    private dev.tinelix.jabwave.xmpp.api.entities.Roster roster;
    private XMPPTCPConnection conn;

    public XMPPService() {
        super("XMPPService");
    }

    public void start(Context context, String server, String username, String password) {
        ctx = context;
        if(status.equals("done")) {
            intent = new Intent(context, XMPPService.class);
            intent.setAction(ACTION_START);
            intent.putExtra(SERVER, server);
            intent.putExtra(USERNAME, username);
            intent.putExtra(PASSWORD, password);
            context.startService(intent);
            Log.d("XMPPService", "Service started.");
        } else {
            Log.w("XMPPService", "Service already started.");
        }
    }

    public void stop(Context context, String server, String username, String password) {
        ctx = context;
        if(intent == null) {
            intent = new Intent(context, getClass());
        }
        intent.setAction(ACTION_STOP);
        stopService(intent);
    }

    public void stop(Context context, Intent intent) {
        ctx = context;
        if(intent == null) {
            intent = new Intent(context, getClass());
        }
        intent.setAction(ACTION_STOP);
        stopService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ((JabwaveApp) getApplicationContext()).xmpp = this;
            final String action = intent.getAction();
            final String server = intent.getStringExtra(SERVER);
            final String username = intent.getStringExtra(USERNAME);
            final String password = intent.getStringExtra(PASSWORD);
            handleAction(action, server, username, password);
        }
    }

    private void handleAction(String action, String server, String username, String password) {
        if(action.equals(ACTION_START)) {
            Log.d("XMPPService", "Preparing...");
            status = "preparing";
            SmackConfiguration.DEBUG = true;
            JabwaveApp app = (JabwaveApp) getApplicationContext();
            try {
                new Thread(() -> {
                    try {
                        createAuthConfig(server, username, password);
                        client = new XMPPClient(conn, new XMPPClient.ApiHandler() {
                            @Override
                            public void onSuccess(XMPPConnection conn, Object object) {
                                receiveState(conn, object);
                            }

                            @Override
                            public void onFail(Throwable t) {
                                receiveState(client.getConnection(), t);
                            }
                        });
                        Log.d(JabwaveApp.XMPP_SERV_TAG, "Authorizing...");
                        status = "authorizing";
                        listenConnection(client);
                        try {
                            client.start(server, username, new String(Base58.decode(password)));
                        } catch (Base58Exception e) {
                            Log.e(JabwaveApp.XMPP_SERV_TAG,
                                    "Authentication with Base58 failed. Retrying with plain password..."
                            );
                            client.start(server, username, password);
                        }
                        Log.d(JabwaveApp.XMPP_SERV_TAG, "Authorized!");
                        status = "authorized";
                        buildHelloPresence(conn);
                        roster = getRoster();
                        sendMessageToActivity(status);
                    } catch (Exception ex) {
                        status = "error";
                        ex.printStackTrace();
                        sendMessageToActivity(status);
                    }
                }).start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if(action.equals(ACTION_STOP)) {
            try {
                conn.disconnect();
                status = "done";
                sendMessageToActivity(status);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createAuthConfig(String server, String jid, String password) {
        XMPPTCPConnectionConfiguration config =
                Authentication.buildAuthConfig(
                        server,
                        jid,
                        password,
                        true
                );
        conn = new XMPPTCPConnection(config);
    }

    private void receiveState(XMPPConnection conn, Object object) {

    }

    private void listenConnection(XMPPClient client) {
        client.listenConnection(new XMPPClient.ApiConnectionHandler() {
            @Override
            public void onSuccess(XMPPConnection conn) {
                status = "connected";
            }

            @Override
            public void onFail(Throwable t) {
                status = "connection_error";
            }

            @Override
            public void onDisconnect() {
                status = "disconnect";
            }
        });
    }

    private void buildHelloPresence(XMPPConnection conn) {

    }

    public Roster getRoster() {
        return client.getRoster();
    }

    public boolean isConnected() {
        if(conn != null) {
            return conn.isConnected();
        } else {
            return false;
        }
    }

    private void sendMessageToActivity(String status) {
        Intent intent = new Intent();
        switch (status) {
            case "error":
                intent.putExtra("msg", HandlerMessages.NO_INTERNET_CONNECTION);
                break;
            case "authorized":
                intent.putExtra("msg", HandlerMessages.AUTHORIZED);
                break;
            case "done":
                intent.putExtra("msg", HandlerMessages.DONE);
                break;
            default:
                intent.putExtra("msg", HandlerMessages.UNKNOWN_ERROR);
                break;
        }
        intent.setAction("dev.tinelix.jabwave.XMPP_RECEIVE");
        sendBroadcast(intent);
    }

    private void sendMessageToActivity(String status, Bundle data) {
        Intent intent = new Intent();
        switch (status) {
            case "presence_changed":
                intent.putExtra("msg", HandlerMessages.ROSTER_CHANGED);
                intent.putExtra("data", data);
                break;
        }
        intent.setAction("dev.tinelix.jabwave.XMPP_RECEIVE");
        sendBroadcast(intent);
    }

    public String getStatus() {
        return status;
    }

    public void stopService() {
        if(conn != null) {
            if (conn.isConnected()) {
                conn.disconnect();
            }
        }
        stopSelf();
    }
}