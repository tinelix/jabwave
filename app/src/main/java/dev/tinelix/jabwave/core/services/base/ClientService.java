package dev.tinelix.jabwave.core.services.base;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.entities.Account;
import dev.tinelix.jabwave.api.base.entities.Authenticator;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.base.models.Chats;
import dev.tinelix.jabwave.api.base.models.Services;
import dev.tinelix.jabwave.core.utilities.NotificationChannel;

public class ClientService extends IntentService {

    private final String network_name;
    protected Authenticator auth;
    protected Account account;
    protected Chats chats;
    protected BaseClient client;
    protected Services services;
    protected int authType;
    protected ClientServiceBinder binder = new ClientServiceBinder();
    private Notification notification;
    private NotificationChannel serviceChannel;

    public class ClientServiceBinder extends Binder {
        public OnClientAPIResultListener listener;
        public ClientService getService() {
            return ClientService.this;
        }

        public BaseClient getClient() {
            return ClientService.this.client;
        }

        public Authenticator getAuthenticator() {
            return ClientService.this.auth;
        }

        public Chats getChats() {
            return ClientService.this.chats;
        }
    }

    /**
     * Dummy mediator service for two or more incompatible API clients.
     */
    public ClientService(@NonNull String name) {
        super(name);
        this.network_name = name;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    public void start(@NonNull Context ctx,
                      ServiceConnection connection,
                      HashMap<String, String> map) {

    }

    public boolean isConnected() {
        return false;
    }

    public Account getAccount() {
        return account;
    }

    public Authenticator getAuthenticator() {
        return auth;
    }

    public Chats getChats() {
        return chats;
    }

    public BaseClient getClient() {
        return client;
    }

    public Services getNetworkServices() {
        return services;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setAuthenticator(Authenticator auth) {
        this.auth = auth;
    }

    public void setChats(Chats chats) {
        this.chats = chats;
    }

    public void createNotification() {
        serviceChannel = NotificationChannel.Builder.getInstance(this)
                .setChannelName("network_updates", "Network Updates")
                .setChannelParameters(false, false, false)
                .setRingtoneUrl(null)
                .build();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = serviceChannel.createNotification(
                    R.drawable.ic_notification_icon,
                    network_name,
                    getResources().getString(R.string.mds_subtitle),
                    true
            );
        } else {
            Log.w(
                    JabwaveApp.APP_TAG,
                    "Background client service notification not needed for Android N and lower."
            );
        }
    }

    public void startForeground() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(Notification.FLAG_FOREGROUND_SERVICE, notification);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        binder = new ClientServiceBinder();
        onBind(intent);
    }

    public Account createAccount() {
        return null;
    }

    public int getAuthType() {
        // returns 0 if username/ID/phone number required
        // also returns 1 if required phone number with authentication code
        return authType;
    }

    public boolean isAsyncAPIs() {
        return getClient().isAsyncApi();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        createNotification();
        startForeground();
        return super.onStartCommand(intent, flags, startId);
    }
}
