package dev.tinelix.jabwave.core.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
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
import com.mediaparkpk.base58android.Base58;
import com.mediaparkpk.base58android.Base58Exception;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.core.fragments.app.ChatsFragment;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;
import dev.tinelix.jabwave.core.services.XMPPService;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.core.utilities.FragmentNavigator;
import dev.tinelix.jabwave.api.base.SecureStorage;
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
            ((JabwaveApp) AppActivity.this.getApplicationContext()).clientService = binder.getService();
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
        setContentView(R.layout.activity_app);
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
        fragment = FragmentNavigator.switchToAnotherFragment(
                getSupportFragmentManager(), R.id.app_fragment, FragmentNavigator.FRAGMENT_CHATS
        );
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
        try {
            String server = app.getXmppPreferences().getString("server", "");
            String username = app.getXmppPreferences().getString("username", "");
            String password = new String(Base58.decode(
                    app.getXmppPreferences().getString("password_hash", "")
            ), StandardCharsets.UTF_8);
            credentials = new SecureStorage().createCredentialsMap(server, username, password);
            service = new XMPPService();
            ((XMPPService) service).start(this, clientConnection, credentials);
        } catch (Base58Exception e) {
            e.printStackTrace();
        }
    }

    private void getAccount() {
        service.createAccount();
        if(!service.isAsyncAPIs()) {
            updateNavView();
            getContacts();
        }
    }

    private void getContacts() {
        if(fragment instanceof ChatsFragment) {
            ((ChatsFragment) fragment).loadContacts();
            findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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
                if (fragment instanceof ChatsFragment) {
                    findViewById(R.id.progress).setVisibility(View.GONE);
                    findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                    ((ChatsFragment) fragment).loadLocalContacts();
                }
                break;
            case HandlerMessages.CHATS_UPDATED:
                if (fragment instanceof ChatsFragment) {
                    ((ChatsFragment) fragment).refreshAdapter();
                }
                break;
        }
    }

    private void updateNavView() {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        View header = navView.getHeaderView(0);
        TextView profile_name = header.findViewById(R.id.profile_name);
        TextView profile_id = header.findViewById(R.id.screen_name);
        ShapeableImageView profile_photo = header.findViewById(R.id.profile_avatar);
        dev.tinelix.jabwave.api.base.entities.Account account = service.getAccount();
        if(app.getCurrentNetworkType().equals("telegram")) {
            profile_name.setText(
                    String.format("%s %s", account.first_name, account.last_name)
            );
            if(account.username != null) {
                profile_id.setText(String.format("@%s", account.username));
            } else {
                profile_id.setVisibility(View.GONE);
            }
        } else {
            profile_name.setText(
                    String.format("%s %s", account.first_name, account.last_name)
            );
            if(account.id != null) {
                profile_id.setText(String.format("%s", account.id));
            } else {
                profile_id.setVisibility(View.GONE);
            }
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.all_chats -> fragment = FragmentNavigator.switchToAnotherFragment(
                    getSupportFragmentManager(), R.id.app_fragment, FragmentNavigator.FRAGMENT_CHATS
            );
            case R.id.services -> fragment = FragmentNavigator.switchToAnotherFragment(
                    getSupportFragmentManager(), R.id.app_fragment, FragmentNavigator.FRAGMENT_SERVICES
            );
            case R.id.about_app -> {
                Intent intent = new Intent(this, AboutAppActivity.class);
                startActivity(intent);
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        service.stopSelf();
        unregisterReceiver(jwReceiver);
        super.onDestroy();
    }

    @Override
    public void handleOnBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!(fragment instanceof ChatsFragment)) {
            fragment = FragmentNavigator.switchToAnotherFragment(
                    getSupportFragmentManager(), R.id.app_fragment, FragmentNavigator.FRAGMENT_CHATS
            );
        } else {
            super.handleOnBackPressed();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
