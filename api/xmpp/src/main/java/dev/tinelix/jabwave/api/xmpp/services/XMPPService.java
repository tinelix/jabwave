package dev.tinelix.jabwave.api.xmpp.services;

import static dev.tinelix.jabwave.api.base.BaseClient.API_CLIENT_TAG;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

import com.mediaparkpk.base58android.Base58;
import com.mediaparkpk.base58android.Base58Exception;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import dev.tinelix.jabwave.api.base.services.ClientService;
import dev.tinelix.jabwave.api.base.entities.Account;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.base.models.Chats;
import dev.tinelix.jabwave.api.xmpp.models.Services;
import dev.tinelix.jabwave.api.base.enums.HandlerMessages;
import dev.tinelix.jabwave.api.xmpp.XMPPClient;
import dev.tinelix.jabwave.api.xmpp.entities.Authenticator;
import dev.tinelix.jabwave.api.xmpp.models.Roster;

/**
 * XMPP (Smack) client service
 */

public class XMPPService extends ClientService {

    public static final String NETWORK_NAME = "XMPP (Smack)";

    private static final String ACTION_START = "start_service";
    private static final String ACTION_STOP = "stop_service";

    private static final String SERVER = "server";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private Context ctx;

    private String status = "done";

    private Intent intent;
    private Roster roster;
    private XMPPTCPConnection conn;

    public XMPPService() {
        super(NETWORK_NAME);
    }

    public class XMPPServiceBinder extends Binder {
        public XMPPService getService() {
            return XMPPService.this;
        }

        public XMPPClient getClient() {
            return (XMPPClient) XMPPService.this.client;
        }

        public Roster getRoster() {
            return XMPPService.this.roster;
        }
    }

    @Override
    public void start(@NonNull Context ctx, ServiceConnection connection, HashMap<String, String> map) {
        this.ctx = ctx;
        String server = map.get("server");
        String username = map.get("username");
        String password = map.get("password");
        if(status.equals("done")) {
            intent = new Intent(ctx, XMPPService.class);
            intent.setAction(ACTION_START);
            intent.putExtra(SERVER, server);
            intent.putExtra(USERNAME, username);
            intent.putExtra(PASSWORD, password);
            ctx.startService(intent);
            ctx.bindService(intent, connection, Context.BIND_AUTO_CREATE);
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
                createAuthConfig(server, username, password);
                client = new XMPPClient(ctx, conn, new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        return true;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                });
                status = "authorizing";
                listenConnection((XMPPClient) client);
                try {
                    Log.d(API_CLIENT_TAG, String.format("Connecting to %s...", server));
                    conn.connect();
                    Log.d(API_CLIENT_TAG, "Authorizing...");
                    auth = new Authenticator((XMPPClient) client);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("jid", String.format("%s@%s", username, server));
                    map.put("password", new String(Base58.decode(password)));
                    auth.signIn(map, new OnClientAPIResultListener() {
                        @Override
                        public boolean onSuccess(HashMap<String, Object> map) {
                            Log.d(API_CLIENT_TAG, "Authorized!");
                            status = "authorized";
                            buildHelloPresence(conn);
                            roster = new Roster(XMPPService.this, ((XMPPClient) client), map1 -> {
                                Log.d(API_CLIENT_TAG, "Presence changed!");
                                sendMessageToActivity("presence_changed");
                                return false;
                            });
                            sendMessageToActivity(status);
                            services = new Services(client);
                            return false;
                        }

                        @Override
                        public boolean onFail(HashMap<String, Object> map, Throwable t) {
                            status = "auth_error";
                            Bundle bundle = new Bundle();
                            bundle.putString("error_msg", client.getConnectionErrorMessage(
                                    Objects.requireNonNull(t.getMessage()))
                            );
                            sendMessageToActivity(status, bundle);
                            return false;
                        }
                    });
                } catch (Base58Exception e) {
                    Log.e(API_CLIENT_TAG,
                            "Authentication with Base58 failed"
                    );
                }
            } catch (Exception ex) {
                status = "auth_error";
                ex.printStackTrace();
                Bundle bundle = new Bundle();
                bundle.putString("error_msg", client.getConnectionErrorMessage(
                        Objects.requireNonNull(ex.getMessage()))
                );
                sendMessageToActivity(status, bundle);
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

    private void createAuthConfig(String server, String jid, String password){
        XMPPTCPConnectionConfiguration config =
                Authenticator.buildAuthConfig(
                        server,
                        jid,
                        password,
                        true
                );
        conn = new XMPPTCPConnection(config);
    }

    private void listenConnection(XMPPClient client) {
        client.listenConnection(new OnClientAPIResultListener() {
            @Override
            public boolean onSuccess(HashMap<String, Object> map) {
                status = "connected";
                return false;
            }

            @Override
            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                return false;
            }
        });
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

    public Roster getRoster() {
        return roster;
    }

    @Override
    public Chats getChats() {
        return getRoster();
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
            case "error" ->
                    intent.putExtra("msg", HandlerMessages.NO_INTERNET_CONNECTION);
            case "auth_error" ->
                    intent.putExtra("msg", HandlerMessages.AUTHENTICATION_ERROR);
            case "authorized" ->
                    intent.putExtra("msg", HandlerMessages.AUTHORIZED);
            case "done" ->
                    intent.putExtra("msg", HandlerMessages.DONE);
            case "presence_changed" ->
                    intent.putExtra("msg", HandlerMessages.CHATS_UPDATED);
            case "account_loaded" ->
                    intent.putExtra("msg", HandlerMessages.ACCOUNT_LOADED);
            default ->
                    intent.putExtra("msg", HandlerMessages.UNKNOWN_ERROR);
        }
        intent.setAction("dev.tinelix.jabwave.XMPP_RECEIVE");
        sendBroadcast(intent);
    }

    private void sendMessageToActivity(String status, Bundle data) {
        Intent intent = new Intent();
        switch (status) {
            case "presence_changed" -> {
                intent.putExtra("msg", HandlerMessages.CHATS_LOADED);
                intent.putExtra("data", data);
            }
            case "auth_error" -> {
                intent.putExtra("msg", HandlerMessages.AUTHENTICATION_ERROR);
                intent.putExtra("data", data);
            }
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

    @Override
    public dev.tinelix.jabwave.api.base.entities.Authenticator getAuthenticator() {
        return auth;
    }

    @Override
    public Account createAccount() {
        account = new dev.tinelix.jabwave.api.xmpp.entities.Account((XMPPClient) client);
        return account;
    }

    @Override
    public int getAuthType() {
        return authType;
    }
}