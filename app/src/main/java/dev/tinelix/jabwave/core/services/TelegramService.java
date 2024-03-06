package dev.tinelix.jabwave.core.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;

import androidx.annotation.NonNull;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;
import dev.tinelix.jabwave.net.telegram.api.entities.Account;
import dev.tinelix.jabwave.net.telegram.api.entities.Authenticator;
import dev.tinelix.jabwave.net.telegram.api.entities.Chat;
import dev.tinelix.jabwave.net.telegram.api.models.Chats;
import dev.tinelix.jabwave.ui.enums.HandlerMessages;

/**
 * <b>Telegram (TDLib) client service</b>. Based on ClientService class.
 */

public class TelegramService extends ClientService implements TDLibClient.ApiHandler, TDLibClient.ClientHandler {

    private static final String ACTION_START = "start_service";
    private static final String ACTION_SEND_CLIENT_CMD = "sendClientCmd";
    private static final String ACTION_STOP = "stop_service";

    private static final String PHONE_NUMBER = "phone_number";
    public Account account;
    private Context ctx;

    private String status = "done";

    private TDLibClient client = null;

    public Chats chats;
    public Authenticator authenticator;

    private Intent intent;

    private String phone_number;
    private boolean isConnected;

    public TelegramService() {
        super(JabwaveApp.TELEGRAM_SERV_TAG);
    }

    @Override
    public void start(@NonNull Context ctx, ServiceConnection connection, HashMap<String, String> map) {
        this.ctx = ctx;
        phone_number = map.get("username");
        if(status.equals("done") || isConnected()) {
            intent = new Intent(ctx, TelegramService.class);
            intent.setAction(ACTION_START);
            intent.putExtra(PHONE_NUMBER, phone_number);
            ctx.startService(intent);
            Log.d(JabwaveApp.TELEGRAM_SERV_TAG, "Service started.");
            ctx.bindService(intent, connection, BIND_AUTO_CREATE);
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
            final String action = intent.getAction();
            final String phone_number = intent.getStringExtra(PHONE_NUMBER);
            authType = 1;
            if(action.equals("ACTION_RUN_FUNCTION")) {
                if(client != null)
                    runClientFunction(
                            intent.getIntExtra("function_constructor", 0),
                            intent.getBundleExtra("function_parameters")
                    );
            } else {
                handleAction(action, phone_number);
            }
        }
    }

    private void handleAction(String action, String phone_number) {
        switch (action) {
            case ACTION_START:
                Log.d(JabwaveApp.TELEGRAM_SERV_TAG, "Preparing...");
                status = "preparing";
                try {
                    this.phone_number = phone_number;
                    this.client = new TDLibClient(getApplicationContext(), this, this);
                    isConnected = true;
                    authenticator = new Authenticator(client);
                    authenticator.checkAuthState();
                    if(!authenticator.isAuthorized())
                        authenticator.checkPhoneNumber(phone_number);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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

    private void runClientFunction(int constructor, Bundle params) {
        TdApi.Function function = client.createFunction(constructor, params);
        client.send(function, new OnClientAPIResultListener() {
            @Override
            public boolean onSuccess(HashMap<String, Object> map) {
                return false;
            }

            @Override
            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                return false;
            }
        });
    }

    private void sendMessageToActivity(String status) {
        Intent intent = new Intent();
        switch (status) {
            case "required_phone_number":
                intent.putExtra("msg", HandlerMessages.AUTHENTICATION_ERROR);
                break;
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
            case "account_loaded":
                intent.putExtra("msg", HandlerMessages.ACCOUNT_LOADED);
                break;
            case "update_chat_status":
                intent.putExtra("msg", HandlerMessages.CHATS_UPDATED);
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
            case "required_auth_code":
                intent.putExtra("msg", HandlerMessages.REQUIRED_AUTH_CODE);
                intent.putExtra("data", data);
                break;
            case "required_cloud_password":
                intent.putExtra("msg", HandlerMessages.REQUIRED_CLOUD_PASSWORD);
                intent.putExtra("data", data);
                break;
            case "auth_error":
                intent.putExtra("msg", HandlerMessages.AUTHENTICATION_ERROR);
                intent.putExtra("data", data);
                break;
            case "presence_changed":
                intent.putExtra("msg", HandlerMessages.CHATS_LOADED);
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

    @SuppressLint("SwitchIntDef")
    @Override
    public void onSuccess(TdApi.Function function, TdApi.Object object) {
        Log.d("TelegramApi", String.format("Ok?_%s", function.getClass().getSimpleName()));
        if(object instanceof TdApi.Ok) {
            if(function.getConstructor() == TdApi.SetAuthenticationPhoneNumber.CONSTRUCTOR
                || function.getConstructor() == TdApi.CheckAuthenticationCode.CONSTRUCTOR
                || function.getConstructor() == TdApi.CheckAuthenticationPassword.CONSTRUCTOR) {
                authenticator.checkAuthState();
                sendMessageToActivity(status);
            }
        }
    }

    @Override
    public void onFail(TdApi.Function function, Throwable throwable) {
        throwable.printStackTrace();
        if(throwable instanceof TDLibClient.Error) {
            status = ((TDLibClient.Error) throwable).getTag();
        }
        Bundle data = new Bundle();
        data.getInt("function", function.getConstructor());
        sendMessageToActivity(status, data);
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public void onUpdate(TdApi.Object object) {
        //Log.d(JabwaveApp.APP_TAG, String.format("Updating data to %s...", object.getClass().getSimpleName()));
        if(object instanceof TdApi.UpdateAuthorizationState) {
            TdApi.AuthorizationState state =
                    ((TdApi.UpdateAuthorizationState) object).authorizationState;
            Log.d(JabwaveApp.APP_TAG, String.format("Updating authorization state to %s...", state.getClass().getSimpleName()));
            switch (state.getConstructor()) {
                case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                    status = "auth_error";
                    break;
                case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                    status = "required_auth_code";
                    break;
                case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR:
                    status = "required_cloud_password";
                    break;
                case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                    authenticator.setAuthState(state);
                    status = "authorized";
                    break;
                default:
                    break;
            }
            sendMessageToActivity(status);
        } else if(object instanceof TdApi.UpdateUserStatus) {
            TdApi.UpdateUserStatus userStatus = ((TdApi.UpdateUserStatus) object);
            Chat chat = null;
            switch(userStatus.status.getConstructor()) {
                case TdApi.UserStatusOnline.CONSTRUCTOR:
                    chat = (Chat) getChats().getChatById(userStatus.userId);
                    chat.status = 1;
                    break;
                case TdApi.UserStatusOffline.CONSTRUCTOR:
                    chat = (Chat) getChats().getChatById(userStatus.userId);
                    chat.status = 0;
                    break;
            }
            getChats().chats.set(getChats().getChatIndex(chat), chat);
            status = "update_chat_status";
            sendMessageToActivity(status);
        }
    }

    @Override
    public TDLibClient getClient() {
        return client;
    }

    @Override
    public Chats getChats() {
        if(chats == null) {
            chats = new Chats(client);
        }
        return chats;
    }



    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public void setAccount(dev.tinelix.jabwave.net.base.api.entities.Account account) {
        this.account = (Account) account;
    }

    @Override
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public dev.tinelix.jabwave.net.base.api.entities.Account createAccount() {
        account = new Account(client,
                new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        sendMessageToActivity("account_loaded");
                        return false;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                }
        );
        return account;
    }

    @Override
    public int getAuthType() {
        return authType;
    }
}