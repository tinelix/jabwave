package dev.tinelix.jabwave.core.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.tdlwrap.TDLibClient;
import dev.tinelix.jabwave.api.tdlwrap.entities.Account;
import dev.tinelix.jabwave.api.tdlwrap.entities.Authenticator;
import dev.tinelix.jabwave.api.tdlwrap.entities.Chat;
import dev.tinelix.jabwave.api.tdlwrap.models.Chats;
import dev.tinelix.jabwave.api.tdlwrap.models.Services;
import dev.tinelix.jabwave.net.telegram.api.SecureStorage;
import dev.tinelix.jabwave.ui.enums.HandlerMessages;

/**
 * <b>Telegram (TDLib) client service</b>. Based on ClientService class.
 */

public class TelegramService extends ClientService implements TDLibClient.ApiHandler, TDLibClient.ClientHandler {

    public static final String NETWORK_NAME = "Telegram (TDLib)";

    private static final String ACTION_START = "start_service";
    private static final String ACTION_SEND_CLIENT_CMD = "sendClientCmd";
    private static final String ACTION_STOP = "stop_service";

    private static final String PHONE_NUMBER = "phone_number";
    private Context ctx;

    private String status = "done";

    private Intent intent;

    private String phone_number;
    private boolean isConnected;

    public TelegramService() {
        super(NETWORK_NAME);
    }

    @Override
    public void start(@NonNull Context ctx, ServiceConnection connection, HashMap<String, String> map) {
        this.ctx = ctx;
        phone_number = map.get("username");
        Log.d("PN", phone_number);
        if(status.equals("done") || isConnected()) {
            AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            intent = new Intent(ctx, TelegramService.class);
            intent.setAction(ACTION_START);
            intent.putExtra(PHONE_NUMBER, phone_number);
            PendingIntent pendingIntent = null;
            // Setting PendingIntent for Android API Level 23 and above
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getService(ctx, 0, intent,
                        PendingIntent.FLAG_MUTABLE);
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getService(ctx, 0, intent,
                        PendingIntent.FLAG_IMMUTABLE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent);
            } else {
                ctx.startService(intent);
            }
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
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
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
                    BaseClient.ClientIdentityParams params = new BaseClient.ClientIdentityParams();
                    SecureStorage storage = new SecureStorage();
                    client = new TDLibClient(
                            getApplicationContext(), this, this, storage, params
                    );
                    isConnected = true;
                    auth = new Authenticator((TDLibClient) client);
                    ((Authenticator) auth).checkAuthState(new OnClientAPIResultListener() {
                        @Override
                        public boolean onSuccess(HashMap<String, Object> map) {
                            if(!auth.isAuthenticated())
                                ((Authenticator) auth).checkPhoneNumber(phone_number);
                            return false;
                        }

                        @Override
                        public boolean onFail(HashMap<String, Object> map, Throwable t) {
                            return false;
                        }
                    });
                    services = new Services(client);
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
        TdApi.Function function = ((TDLibClient) client).createFunction(constructor, params);
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
            case "required_phone_number" ->
                    intent.putExtra("msg", HandlerMessages.AUTHENTICATION_ERROR);
            case "error" ->
                    intent.putExtra("msg", HandlerMessages.NO_INTERNET_CONNECTION);
            case "required_auth_code" ->
                    intent.putExtra("msg", HandlerMessages.REQUIRED_AUTH_CODE);
            case "required_cloud_password" ->
                    intent.putExtra("msg", HandlerMessages.REQUIRED_CLOUD_PASSWORD);
            case "authorized" ->
                    intent.putExtra("msg", HandlerMessages.AUTHORIZED);
            case "account_loaded" ->
                    intent.putExtra("msg", HandlerMessages.ACCOUNT_LOADED);
            case "update_chat_status" ->
                    intent.putExtra("msg", HandlerMessages.CHATS_UPDATED);
            case "done" ->
                    intent.putExtra("msg", HandlerMessages.DONE);
            default ->
                    intent.putExtra("msg", HandlerMessages.UNKNOWN_ERROR);
        }
        intent.setAction("dev.tinelix.jabwave.TELEGRAM_RECEIVE");
        sendBroadcast(intent);
    }

    private void sendMessageToActivity(String status, Bundle data) {
        Intent intent = new Intent();
        switch (status) {
            case "required_auth_code" ->
                    intent.putExtra("msg", HandlerMessages.REQUIRED_AUTH_CODE);
            case "required_cloud_password" ->
                    intent.putExtra("msg", HandlerMessages.REQUIRED_CLOUD_PASSWORD);
            case "auth_error" ->
                    intent.putExtra("msg", HandlerMessages.AUTHENTICATION_ERROR);
            case "update_file" ->
                    intent.putExtra("msg", HandlerMessages.FILES_UPDATED);
            case "presence_changed" ->
                    intent.putExtra("msg", HandlerMessages.CHATS_LOADED);
        }
        intent.putExtra("data", data);
        intent.setAction("dev.tinelix.jabwave.TELEGRAM_RECEIVE");
        sendBroadcast(intent);
    }

    public String getStatus() {
        return status;
    }

    public void stopService() {
        if(isConnected()) {
            ((TDLibClient) client).destroy();
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
                ((Authenticator) auth).checkAuthState(new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        sendMessageToActivity(status);
                        return false;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                });
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
                case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR ->
                        status = "auth_error";
                case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR ->
                        status = "required_auth_code";
                case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR ->
                        status = "required_cloud_password";
                case TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                    ((Authenticator) auth).setAuthState(state);
                    status = "authorized";
                }
                default -> {
                }
            }
            sendMessageToActivity(status);
        } else if(object instanceof TdApi.UpdateUserStatus userStatus) {
            Chat chat = null;
            if(getChats().getChatById(userStatus.userId) instanceof Chat) {
                switch (userStatus.status.getConstructor()) {
                    case TdApi.UserStatusOnline.CONSTRUCTOR -> {
                        chat = (Chat) getChats().getChatById(userStatus.userId);
                        if (chat != null)
                            chat.status = 1;
                    }
                    case TdApi.UserStatusOffline.CONSTRUCTOR -> {
                        chat = (Chat) getChats().getChatById(userStatus.userId);
                        if (chat != null)
                            chat.status = 0;
                    }
                }
                if (chat != null) {
                    if (getChats().getChatIndex(chat) >= 0) {
                        getChats().chats.set(getChats().getChatIndex(chat), chat);
                        status = "update_chat_status";
                        sendMessageToActivity(status);
                    }
                }
            }
        } else if(object instanceof TdApi.UpdateFile updateFile) {
            Bundle data = new Bundle();
            boolean updatingCompleted = updateFile.file.local.isDownloadingCompleted
                    || updateFile.file.remote.isUploadingCompleted;
            boolean isUpload = updateFile.file.remote.isUploadingActive
                    || updateFile.file.remote.isUploadingCompleted;
            data.putInt("file_id", updateFile.file.id);
            data.putLong("updateSize", updateFile.file.local.downloadedSize);
            data.putLong("fullSize", updateFile.file.size);
            data.putBoolean("updatingCompleted", updatingCompleted);
            data.putBoolean("isUpload", isUpload);
            if(updatingCompleted) {
                if (isUpload)
                    Log.d(JabwaveApp.TELEGRAM_SERV_TAG,
                            String.format(
                                    "Upload completed (File ID #%s, %s): %s/%s KB...",
                                    updateFile.file.id,
                                    updateFile.file.local.path,
                                    updateFile.file.local.downloadedSize / 1024,
                                    updateFile.file.size / 1024
                            )
                    );
                else
                    Log.d(JabwaveApp.TELEGRAM_SERV_TAG,
                            String.format(
                                    "Download completed (File ID #%s, %s): %s/%s KB...",
                                    updateFile.file.id,
                                    updateFile.file.local.path,
                                    updateFile.file.local.downloadedSize / 1024,
                                    updateFile.file.size / 1024
                            )
                    );
            } else {
                if (isUpload)
                    Log.d(JabwaveApp.TELEGRAM_SERV_TAG,
                            String.format(
                                    "Uploading file #%s: %s/%s KB...",
                                    updateFile.file.id,
                                    updateFile.file.local.downloadedSize / 1024,
                                    updateFile.file.size / 1024
                            )
                    );
                else
                    Log.d(JabwaveApp.TELEGRAM_SERV_TAG,
                            String.format(
                                    "Downloading file #%s: %s/%s KB...",
                                    updateFile.file.id,
                                    updateFile.file.local.downloadedSize / 1024,
                                    updateFile.file.size / 1024
                            )
                    );
            }
            status = "update_file";
            sendMessageToActivity(status, data);
        }
    }

    @Override
    public Chats getChats() {
        if(chats == null) {
            chats = new Chats(((TDLibClient) client));
        }
        return (Chats) chats;
    }

    @Override
    public dev.tinelix.jabwave.api.base.entities.Account createAccount() {
        account = new Account(((TDLibClient) client),
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