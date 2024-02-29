package dev.tinelix.jabwave.xmpp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;

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
import dev.tinelix.jabwave.xmpp.api.XMPPAuthorization;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.xmpp.api.entities.Roster;
import dev.tinelix.jabwave.xmpp.enumerations.HandlerMessages;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */

public class XMPPService extends IntentService {

    private static final String ACTION_START = "start_service";
    private static final String ACTION_STOP = "stop_service";

    private static final String SERVER = "server";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private Context ctx;

    private String status = "done";

    private AbstractXMPPConnection conn;
    private Messenger serviceMessenger;
    private Messenger activityMessenger;
    private ConnectionListener connListener;
    private Intent intent;
    private dev.tinelix.jabwave.xmpp.api.entities.Roster roster;

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
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            XMPPTCPConnectionConfiguration config =
                                    XMPPAuthorization.buildConnectionConfig(
                                            server,
                                            username,
                                            password,
                                            true
                                    );
                            conn = new XMPPTCPConnection(config);
                            try {
                                listenConnection();
                                conn.connect();
                                Log.d(JabwaveApp.XMPP_SERV_TAG, "Authorizing...");
                                status = "authorizing";
                                conn.login(
                                        username,
                                        password,
                                        XMPPAuthorization.generateXMPPResource()
                                );
                                Log.d(JabwaveApp.XMPP_SERV_TAG, "Authorized!");
                                status = "authorized";
                                buildHelloPresence(conn);
                                roster = getRoster();
                                sendMessageToActivity(status);
                            } catch (Exception ex) {
                                if (ex.getClass().getSimpleName().equals("EndpointConnectionException")) {
                                    status = "error";
                                }
                                ex.printStackTrace();
                                sendMessageToActivity(status);
                            }
                        } catch (Exception ex) {
                            status = "error";
                            ex.printStackTrace();
                            sendMessageToActivity(status);
                        }
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

    private void buildHelloPresence(XMPPConnection conn) {
        try {
            Presence presence = conn
                .getStanzaFactory()
                .buildPresenceStanza()
                .setMode(Presence.Mode.available)
                .ofType(Presence.Type.available)
                .build();
            conn.sendStanza(presence);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private Roster getRoster() {
        return new Roster(conn);
    }

    public ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = null;
        if(conn != null) {
            if (conn.isConnected() && conn.isAuthenticated()) {
                contacts = roster.getContacts();
            }
        }
        status = "getting_contacts_list";
        sendMessageToActivity(status);
        return contacts;
    }

    public ArrayList<Contact> getChatGroups() {
        ArrayList<Contact> groups = new ArrayList<>();
        if(conn != null) {
            if (conn.isConnected() && conn.isAuthenticated()) {
                groups = roster.getGroups();
            }
        }
        return groups;
    }

    public boolean isConnected() {
        if(conn != null) {
            return conn.isConnected();
        } else {
            return false;
        }
    }

    private void listenConnection() {
        if(connListener != null) {
            conn.removeConnectionListener(connListener);
        }
        connListener = new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                ConnectionListener.super.connected(connection);
                Log.d(JabwaveApp.XMPP_SERV_TAG, "Connected!");
                status = "connected";
            }

            @Override
            public void connecting(XMPPConnection connection) {
                ConnectionListener.super.connecting(connection);
                Log.d(JabwaveApp.XMPP_SERV_TAG,
                        String.format("Connecting to %s...", connection.getXMPPServiceDomain())
                );
                status = "connecting";
            }

            @Override
            public void connectionClosed() {
                ConnectionListener.super.connectionClosed();
                Log.d("XMPPService", "Disconnected");
                status = "disconnected";
                sendMessageToActivity(status);
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                ConnectionListener.super.connectionClosedOnError(e);
                Log.d(
                        "XMPPService", "Connection error: " + e.getMessage()
                );
                status = "connection_error";
                sendMessageToActivity(status);
            }
        };
        conn.addConnectionListener(connListener);
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