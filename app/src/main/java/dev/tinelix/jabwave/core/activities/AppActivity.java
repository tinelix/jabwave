package dev.tinelix.jabwave.core.activities;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.core.fragments.app.ContactsListFragment;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;
import dev.tinelix.jabwave.core.services.TelegramService;
import dev.tinelix.jabwave.core.services.XMPPService;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.net.base.SecureStorage;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;
import dev.tinelix.jabwave.net.telegram.api.entities.Account;
import dev.tinelix.jabwave.net.xmpp.api.models.Roster;
import dev.tinelix.jabwave.ui.enums.HandlerMessages;
import dev.tinelix.jabwave.ui.list.adapters.ChatsAdapter;
import dev.tinelix.jabwave.ui.views.base.JabwaveActionBar;

public class AppActivity extends JabwaveActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ChatsAdapter chatsAdapter;
    private Fragment fragment;
    private FragmentTransaction ft;
    private DrawerLayout drawer;

    private JabwaveReceiver jwReceiver;
    private JabwaveApp app;
    public ClientService service;

    private final ServiceConnection clientConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ClientService.ClientServiceBinder binder = (ClientService.ClientServiceBinder) service;
            AppActivity.this.service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            AppActivity.this.service.stopSelf();
            AppActivity.this.service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = ((JabwaveApp) getApplicationContext());
        findViewById(R.id.app_fragment).setVisibility(View.GONE);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        registerBroadcastReceiver();
        if(service == null) {
            service = new ClientService(app.getCurrentNetworkType());
        }
        if(!service.isConnected()) {
            connect();
        } else {
            getAccount();
        }
        createMainFragment();
        setActionBar();
        drawer = findViewById(R.id.drawer_layout);
    }

    private void setActionBar() {
        JabwaveActionBar actionbar = findViewById(R.id.actionbar);
        actionbar.setNavigationIconTint(R.color.white);
        setSupportActionBar(actionbar);
    }

    private void createMainFragment() {
        fragment = new ContactsListFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.app_fragment, fragment);
        ft.commit();
    }

    private void registerBroadcastReceiver() {
        jwReceiver = new JabwaveReceiver(this);
        if(app.getCurrentNetworkType().equals("telegram")) {
            registerReceiver(
                    jwReceiver,
                    new IntentFilter("dev.tinelix.jabwave.TELEGRAM_RECEIVE")
            );
        } else {
            registerReceiver(
                    jwReceiver,
                    new IntentFilter("dev.tinelix.jabwave.XMPP_RECEIVE")
            );
        }
    }

    private void connect() {
        HashMap<String, String> credentials;
        if(app.getCurrentNetworkType().equals("telegram")) {
            credentials = new SecureStorage().createCredentialsMap(
                    app.getTelegramPreferences().getString("phone_number", "")
            );
            ((TelegramService) service).start(this, credentials);
        } else {
            credentials = new SecureStorage().createCredentialsMap(
                    app.getXmppPreferences().getString("server", ""),
                    app.getXmppPreferences().getString("username", ""),
                    app.getXmppPreferences().getString("password_hash", "")
            );
            ((XMPPService) service).start(this, credentials);
        }

    }

    private void getAccount() {
        if(app.getCurrentNetworkType().equals("telegram")) {
            service.setAccount(
                    new Account(
                            (TDLibClient) service.getClient(),
                            new OnClientAPIResultListener() {
                                    @Override
                                    public boolean onSuccess(HashMap<String, Object> map) {
                                        Global.triggerReceiverIntent(
                                                AppActivity.this,
                                                HandlerMessages.ACCOUNT_LOADED
                                        );
                                        return false;
                                    }

                                    @Override
                                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                                        return false;
                                    }
                            }
                    )
            );
        } else {
            Global.triggerReceiverIntent(
                    AppActivity.this,
                    HandlerMessages.ACCOUNT_LOADED
            );
        }
    }

    private void getContacts() {
        if(fragment instanceof ContactsListFragment) {
            ((ContactsListFragment) fragment).loadContacts();
            findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
        }
    }

    public void receiveState(int message, Bundle data) {
        switch (message) {
            case HandlerMessages.AUTHORIZED:
                Log.d(JabwaveApp.APP_TAG, "Loading account...");
                getAccount();
                break;
            case HandlerMessages.ACCOUNT_LOADED:
                updateNavView();
                getContacts();
                break;
            case HandlerMessages.CHATS_LOADED:
                if(app.getCurrentNetworkType().equals("telegram")) {
                    if (fragment instanceof ContactsListFragment) {
                        findViewById(R.id.progress).setVisibility(View.GONE);
                        findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                        ((ContactsListFragment) fragment).loadLocalContacts();
                    }
                } else {
                    findViewById(R.id.progress).setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    ((ContactsListFragment) fragment).loadLocalContacts();
                }
        }
    }

    private void updateNavView() {
        NavigationView navView = findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0);
        TextView profile_name = header.findViewById(R.id.profile_name);
        TextView profile_id = header.findViewById(R.id.screen_name);
        ShapeableImageView profile_photo = header.findViewById(R.id.profile_avatar);
        if(app.getCurrentNetworkType().equals("telegram")) {
            Account account = (Account) service.getAccount();
            profile_name.setText(
                    String.format("%s %s", account.first_name, account.last_name)
            );
            if(account.username != null) {
                profile_id.setText(String.format("@%s", account.username));
            } else {
                profile_id.setVisibility(View.GONE);
            }
        } else {
            Roster roster = (Roster) service.getChats();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    protected void onDestroy() {
        service.stopSelf();
        service.stopSelf();
        unregisterReceiver(jwReceiver);
        super.onDestroy();
    }

    @Override
    public void handleOnBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.handleOnBackPressed();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}