package dev.tinelix.jabwave.telegram.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;
import org.jivesoftware.smack.SmackConfiguration;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.telegram.api.TDLibClient;
import dev.tinelix.jabwave.telegram.api.entities.Authentication;
import dev.tinelix.jabwave.telegram.enumerations.HandlerMessages;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */

public class TelegramService extends IntentService {

    private static final String ACTION_START = "start_service";
    private static final String ACTION_SEND_CLIENT_CMD = "sendClientCmd";
    private static final String ACTION_STOP = "stop_service";

    private static final String PHONE_NUMBER = "phone_number";

    private Context ctx;

    private String status = "done";

    private TDLibClient client = null;
    public Authentication authorization;

    private Intent intent;

    private String phone_number;
    private boolean isConnected;

    public TelegramService() {
        super(JabwaveApp.TELEGRAM_SERV_TAG);
    }

    public void start(Context context, String phone_number) {
        ctx = context;
        if(status.equals("done") || isConnected()) {
            intent = new Intent(context, TelegramService.class);
            intent.setAction(ACTION_START);
            intent.putExtra(PHONE_NUMBER, phone_number);
            context.startService(intent);
            Log.d(JabwaveApp.TELEGRAM_SERV_TAG, "Service started.");
        } else {
            Log.w(JabwaveApp.TELEGRAM_SERV_TAG, "Service already started.");
        }
    }

    public void stop(Context context) {
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
            ((JabwaveApp) getApplicationContext()).telegram = this;
            final String action = intent.getAction();
            final String phone_number = intent.getStringExtra(PHONE_NUMBER);
            handleAction(action, phone_number);
        }
    }

    private void handleAction(String action, String phone_number) {
        switch (action) {
            case ACTION_START:
                Log.d(JabwaveApp.TELEGRAM_SERV_TAG, "Preparing...");
                status = "preparing";
                SmackConfiguration.DEBUG = true;
                JabwaveApp app = (JabwaveApp) getApplicationContext();
                try {
                    this.phone_number = phone_number;
                    new Thread(() -> {
                        client = new TDLibClient(getApplicationContext());
                        isConnected = true;
                        Authentication authentication = new Authentication(client, new TDLibClient.ApiHandler() {
                            @Override
                            public void onSuccess(TdApi.Object object) {
                                if(object instanceof TdApi.UpdateAuthorizationState) {
                                    status = "required_auth_code";
                                    sendMessageToActivity(status);
                                }
                            }

                            @Override
                            public void onFail(Throwable throwable) {
                                status = "auth_error";
                                Bundle data = new Bundle();
                                sendMessageToActivity(status, data);
                            }
                        });
                        authentication.checkPhoneNumber(phone_number);
                    }).start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case ACTION_SEND_CLIENT_CMD:

                break;
            case ACTION_STOP:
                try {
                    status = "done";
                    sendMessageToActivity(status);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
        }
    }
/*
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
 */

    private void sendMessageToActivity(String status) {
        Intent intent = new Intent();
        switch (status) {
            case "error":
                intent.putExtra("msg", HandlerMessages.NO_INTERNET_CONNECTION);
                break;
            case "required_auth_code":
                intent.putExtra("msg", HandlerMessages.REQUIRED_AUTH_CODE);
                break;
            case "required_cloud_password":
                intent.putExtra("msg", HandlerMessages.REQUIRED_CLOUD_PASSWORD);
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
        intent.setAction("dev.tinelix.jabwave.TELEGRAM_RECEIVE");
        sendBroadcast(intent);
    }

    private void sendMessageToActivity(String status, Bundle data) {
        Intent intent = new Intent();
        switch (status) {
            case "auth_error":
                intent.putExtra("msg", HandlerMessages.AUTHENTICATION_ERROR);
                intent.putExtra("data", data);
                break;
            case "presence_changed":
                intent.putExtra("msg", HandlerMessages.ROSTER_CHANGED);
                intent.putExtra("data", data);
                break;
        }
        intent.setAction("dev.tinelix.jabwave.TELEGRAM_RECEIVE");
        sendBroadcast(intent);
    }

    public String getStatus() {
        return status;
    }

    public void stopService() {
        if(isConnected()) {
            client.destroy();
            stopSelf();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}