package dev.tinelix.jabwave.core.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.telegram.api.TDLibClient;
import dev.tinelix.jabwave.telegram.api.entities.Authentication;
import dev.tinelix.jabwave.telegram.api.entities.Chat;
import dev.tinelix.jabwave.telegram.api.entities.ChatsList;
import dev.tinelix.jabwave.telegram.enumerations.HandlerMessages;

/**
 * Telegram (TDLib) client service
 */

public class TelegramService extends IntentService implements TDLibClient.ApiHandler, TDLibClient.ClientHandler {

    private static final String ACTION_START = "start_service";
    private static final String ACTION_SEND_CLIENT_CMD = "sendClientCmd";
    private static final String ACTION_STOP = "stop_service";

    private static final String PHONE_NUMBER = "phone_number";
    private Context ctx;

    private String status = "done";

    private TDLibClient client = null;

    public ChatsList chatsList;
    public Authentication authentication;

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
                JabwaveApp app = (JabwaveApp) getApplicationContext();
                try {
                    this.phone_number = phone_number;
                    new Thread(() -> {
                        client = new TDLibClient(getApplicationContext(), this, this);
                        client.sendTdlibParameters();
                        isConnected = true;
                        authentication = new Authentication(client);
                        authentication.checkPhoneNumber(phone_number);
                    }).start();
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
        client.send(function, new TDLibClient.ApiHandler() {
            @Override
            public void onSuccess(TdApi.Function function, TdApi.Object object) {

            }

            @Override
            public void onFail(TdApi.Function function, Throwable throwable) {

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
                authentication.checkAuthState();
                sendMessageToActivity(status);
            }
        }
    }

    @Override
    public void onFail(TdApi.Function function, Throwable throwable) {
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
                    authentication.setAuthState(state);
                    status = "authorized";
                    break;
                default:
                    break;
            }
            sendMessageToActivity(status);
        } else if(object instanceof TdApi.UpdateUserStatus) {
            TdApi.UpdateUserStatus updus = (TdApi.UpdateUserStatus) object;
            for (int i = 0; i < chatsList.chats.size(); i++) {
                if(((Chat) chatsList.chats.get(i)).id == updus.userId) {
                    Chat chat = ((Chat) chatsList.chats.get(i));
                    chat.status = updus.status.getConstructor() == TdApi.UserStatusOnline.CONSTRUCTOR ? 0 : 1;
                    chatsList.chats.set(i, chat);
                }
            }
        }
    }

    public TDLibClient getClient() {
        return client;
    }
}