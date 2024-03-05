package dev.tinelix.jabwave.core.services;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

import com.mediaparkpk.base58android.Base58;
import com.mediaparkpk.base58android.Base58Exception;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.util.HashMap;

import androidx.annotation.NonNull;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.net.base.api.entities.Account;
import dev.tinelix.jabwave.net.base.api.models.Chats;
import dev.tinelix.jabwave.ui.enums.HandlerMessages;
import dev.tinelix.jabwave.net.xmpp.api.XMPPClient;
import dev.tinelix.jabwave.net.xmpp.api.entities.Authenticator;
import dev.tinelix.jabwave.net.xmpp.api.models.Roster;

/**
 * XMPP (Smack) client service
 */

public class XMPPService extends ClientService {

    private static final String ACTION_START = "start_service";
    private static final String ACTION_STOP = "stop_service";

    private static final String SERVER = "server";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private Context ctx;

    private String status = "done";

    private XMPPClient client;
    private Intent intent;
    private Roster roster;
    private XMPPTCPConnection conn;
    private dev.tinelix.jabwave.net.xmpp.api.entities.Account account;

    public XMPPService() {
        super("XMPPService");
    }

    public class XMPPServiceBinder extends Binder {
        public XMPPClient.ApiHandler handler;
        public XMPPService getService() {
            return XMPPService.this;
        }

        public XMPPClient getClient() {
            return XMPPService.this.client;
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
            ctx.bindService(intent, connection, BIND_AUTO_CREATE);
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
            JabwaveApp app = (JabwaveApp) getApplicationContext();
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
                roster = new Roster(client);
                sendMessageToActivity(status);
            } catch (Exception ex) {
                status = "error";
                ex.printStackTrace();
                sendMessageToActivity(status);
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
            case "error":
                intent.putExtra("msg", HandlerMessages.NO_INTERNET_CONNECTION);
                break;
            case "authorized":
                intent.putExtra("msg", HandlerMessages.AUTHORIZED);
                break;
            case "done":
                intent.putExtra("msg", HandlerMessages.DONE);
                break;
            case "account_loaded":
                intent.putExtra("msg", HandlerMessages.ACCOUNT_LOADED);
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
                intent.putExtra("msg", HandlerMessages.CHATS_LOADED);
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

    @Override
    public dev.tinelix.jabwave.net.xmpp.api.entities.Account getAccount() {
        return account;
    }

    @Override
    public Account createAccount() {
        account = new dev.tinelix.jabwave.net.xmpp.api.entities.Account(client);
        return account;
    }

    @Override
    public int getAuthType() {
        return authType;
    }
}