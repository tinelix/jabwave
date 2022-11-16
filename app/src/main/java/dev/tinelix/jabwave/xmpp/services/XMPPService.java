package dev.tinelix.jabwave.xmpp.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Entity;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.user_interface.activities.AuthActivity;
import dev.tinelix.jabwave.user_interface.activities.MainActivity;
import dev.tinelix.jabwave.user_interface.list_items.EntityList;
import dev.tinelix.jabwave.xmpp.enumerations.HandlerMessages;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
@SuppressWarnings("deprecation")
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
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder()
                                    .setUsernameAndPassword(username, password)
                                    .setXmppDomain(server)
                                    .setHost(server)
                                    .setPort(5222)
                                    .setLanguage(Locale.getDefault())
                                    .setSendPresence(false)
                                    .setCompressionEnabled(false)
                                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required);
                            TLSUtils.setEnabledTlsProtocolsToRecommended(configBuilder);
                            SSLContext sc = SSLContext.getInstance("TLS");
                            @SuppressLint("CustomX509TrustManager") TrustManager[] trustAllCerts = new TrustManager[]{
                                    new X509TrustManager() {
                                        @Override
                                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                                        }

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
                            configBuilder.setCustomSSLContext(sc);
                            XMPPTCPConnectionConfiguration config = configBuilder.build();

                            conn = new XMPPTCPConnection(config);
                            try {
                                listenConnection();
                                conn.connect();
                                Log.d("XMPPService", "Authorizing...");
                                status = "authorizing";
                                conn.login();
                                Log.d("XMPPService", "OK!");
                                status = "authorized";
                                sendMessageToActivity(status);
                            } catch (Exception ex) {
                                if (ex.getClass().getSimpleName().equals("EndpointConnectionException")) {
                                    status = "error";
                                }
                                sendMessageToActivity(status);
                            }
                        } catch (Exception ex) {
                            status = "error";
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

    public ArrayList<EntityList> getConversations() {
        ArrayList<EntityList> els = new ArrayList<>();
        if(conn != null) {
            if (conn.isConnected() && conn.isAuthenticated()) {
                Roster roster = Roster.getInstanceFor(conn);
                Collection<RosterEntry> entries = roster.getEntries();
                Collection<RosterGroup> groups = roster.getGroups();
                status = "getting_conversations_list";
                sendMessageToActivity(status);
                String previous_group_name = "";
                els.add(new EntityList(0, getResources().getString(R.string.general_category)));
                for (RosterEntry entry : entries) {
                    for (RosterGroup group : groups) {
                        if(entry.getGroups().size() > 0) {
                            if(entry.getGroups().get(0).equals(group) && !group.getName().equals(previous_group_name)) {
                                previous_group_name = group.getName();
                                els.add(new EntityList(0, group.getName()));
                            }
                        }
                    }
                    if(entry.getName() != null) {
                        els.add(new EntityList(1, entry.getName()));
                    } else {
                        els.add(new EntityList(1, entry.getJid().toString()));
                    }
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
                Log.d("XMPPService", "Connected!");
                status = "connected";
            }

            @Override
            public void connecting(XMPPConnection connection) {
                ConnectionListener.super.connecting(connection);
                Log.d("XMPPService", String.format("Connecting to %s...", connection.getXMPPServiceDomain()));
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
                Log.d("XMPPService", "Connection error: " + e.getMessage());
                status = "connection_error";
                sendMessageToActivity(status);
            }
        };
        conn.addConnectionListener(connListener);
    }

    private void sendMessageToActivity(String status) {
        Intent intent = new Intent();
        if(status.equals("error")) {
            intent.putExtra("msg", HandlerMessages.NO_INTERNET_CONNECTION);
        } else if(status.equals("authorized")) {
            intent.putExtra("msg", HandlerMessages.AUTHORIZED);
        } else if(status.equals("done")) {
            intent.putExtra("msg", HandlerMessages.DONE);
        } else {
            intent.putExtra("msg", HandlerMessages.UNKNOWN_ERROR);
        }
        intent.setAction("dev.tinelix.jabwave.XMPP_RECEIVE");
        sendBroadcast(intent);
    }

    public void setParentContext(Context ctx) {
        this.ctx = ctx;
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