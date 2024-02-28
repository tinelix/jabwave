package dev.tinelix.jabwave.xmpp.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.MessageOrPresenceBuilder;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PresenceBuilder;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.SslContextFactory;
import org.jivesoftware.smack.util.TLSUtils;
import org.jivesoftware.smack.util.ToStringUtil;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jxmpp.jid.Jid;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.xmpp.api.XMPPAuthorization;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.xmpp.api.models.Presences;
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
    private Roster roster;

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
                .build();
            conn.sendStanza(presence);
            ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(conn);
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private Roster getRoster() {
        Roster roster = Roster.getInstanceFor(conn);
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {

            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {

            }

            @Override
            public void presenceChanged(Presence presence) {
                Bundle data = new Bundle();
                data.putInt("presence_priority", presence.getPriority());
                data.putString("presence_status", presence.getStatus());
                data.putString("presence_element_name", presence.getElementName());
                data.putString("presence_jid", presence.getFrom().toString());
                Log.d(JabwaveApp.XMPP_SERV_TAG,
                        String.format(
                                "Changed presence in roster." +
                                "\r\n" +
                                "\r\n\"%s\" (%s)" +
                                "\r\nFrom: %s",
                                presence.getStatus(),
                                presence.getPriority(),
                                presence.getFrom().toString()
                        )
                );
                sendMessageToActivity("presence_changed", data);
            }
        });
        return roster;
    }

    public ArrayList<Contact> getContacts() {
        ArrayList<Contact> els = new ArrayList<>();
        if(conn != null) {
            if (conn.isConnected() && conn.isAuthenticated()) {
                Collection<RosterEntry> entries = roster.getEntries();
                Collection<RosterGroup> groups = roster.getGroups();
                for (RosterEntry entry : entries) {
                    Contact entity = new Contact("");
                    entity.jid = entry.getJid().toString();
                    List<Presence> presences = roster.getAllPresences(entry.getJid());
                    String custom_status = "";
                    int status = 0;
                    if(presences.size() > 0) {
                        Presences presencesModel = new Presences(entity, presences);
                        Presence hpPresence = presencesModel.getHighestPriorityPresence();
                        if(hpPresence != null) {
                            custom_status = hpPresence.getStatus();
                            status = presencesModel.getStatusEnum(hpPresence);
                        }
                    }
                    if(entry.getName() != null) {
                        if(custom_status != null)
                            entity = new Contact(
                                    entry.getName(),
                                    entry.getJid().toString(),
                                    new ArrayList<>(),
                                    custom_status
                            );
                        else
                            entity = new Contact(
                                    entry.getName(),
                                    entry.getJid().toString(),
                                    new ArrayList<>(),
                                    status
                            );
                    } else {
                        if(custom_status != null)
                            entity = new Contact(
                                    entry.getJid().toString(),
                                    entry.getJid().toString(),
                                    new ArrayList<>(),
                                    custom_status
                            );
                        else
                            entity = new Contact(
                                    entry.getJid().toString(),
                                    entry.getJid().toString(),
                                    new ArrayList<>(),
                                    status
                            );
                    }

                    for (RosterGroup group : groups) {
                        if(entry.getGroups().contains(group)) {
                            entity.groups.add(group.getName());
                        }
                    }

                    els.add(entity);
                }
            }
        }
        status = "getting_contacts_list";
        sendMessageToActivity(status);
        return els;
    }

    public ArrayList<Contact> getChatGroups() {
        ArrayList<Contact> els = new ArrayList<>();
        if(conn != null) {
            if (conn.isConnected() && conn.isAuthenticated()) {
                Collection<RosterGroup> groups = roster.getGroups();
                for (RosterGroup group : groups) {
                    els.add(new Contact(group.getName()));
                }
            }
        }
        return els;
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