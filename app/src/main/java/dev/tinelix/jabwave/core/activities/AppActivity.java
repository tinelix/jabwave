package dev.tinelix.jabwave.core.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.drinkless.td.libcore.telegram.TdApi;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.ui.enumerations.HandlerMessages;
import dev.tinelix.jabwave.core.ui.list.adapters.ChatsAdapter;
import dev.tinelix.jabwave.core.fragments.app.ContactsListFragment;
import dev.tinelix.jabwave.core.ui.views.base.JabwaveActionBar;
import dev.tinelix.jabwave.telegram.api.TDLibClient;
import dev.tinelix.jabwave.telegram.api.entities.Account;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;

public class AppActivity extends JabwaveActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ChatsAdapter chatsAdapter;
    private Fragment fragment;
    private FragmentTransaction ft;
    private DrawerLayout drawer;

    private JabwaveReceiver jwReceiver;
    private JabwaveApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = ((JabwaveApp) getApplicationContext());
        findViewById(R.id.app_fragment).setVisibility(View.GONE);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        registerBroadcastReceiver();
        if(!app.xmpp.isConnected()) {
            connect();
        } else {
            getAccount();
        }
        createMainFragment();
        setActionBar();
        drawer = findViewById(R.id.drawer_layout);
        getOnBackPressedDispatcher().addCallback(
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        AppActivity.this.handleOnBackPressed();
                    }
                }
        );
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
        if(app.getCurrentNetworkType().equals("telegram")) {
            if(!app.telegram.isConnected()) {
                app.telegram.start(
                        this,
                        app.getTelegramPreferences().getString("phone_number", "")
                );
            }
        } else {
            if (!app.xmpp.isConnected()) {
                app.xmpp.start(
                        this,
                        app.getXmppPreferences().getString("server", ""),
                        app.getXmppPreferences().getString("username", ""),
                        app.getXmppPreferences().getString("password_hash", "")
                );
            }
        }
    }

    private void getAccount() {
        app.telegram.account = new Account(app.telegram.getClient(), new TDLibClient.ApiHandler() {
            @Override
            public void onSuccess(TdApi.Function function, TdApi.Object object) {
                Global.triggerReceiverIntent(
                        AppActivity.this,
                        dev.tinelix.jabwave.core.ui.enumerations.HandlerMessages.ACCOUNT_LOADED
                );
            }

            @Override
            public void onFail(TdApi.Function function, Throwable throwable) {

            }
        });
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
                    String jid = data.getString("presence_jid");
                    Contact contact = chatsAdapter.searchByJid(jid);
                    if (contact != null) {
                        contact.custom_status = data.getString("presence_status");
                        chatsAdapter.setByJid(jid.split("/")[0], contact);
                    }
                }
        }
    }

    private void updateNavView() {
        NavigationView navView = findViewById(R.id.nav_view);
        View header = navView.getHeaderView(0);
        TextView profile_name = header.findViewById(R.id.profile_name);
        TextView profile_id = header.findViewById(R.id.screen_name);
        if(app.getCurrentNetworkType().equals("telegram")) {
            Account account = app.telegram.account;
            profile_name.setText(
                    String.format("%s %s", account.first_name, account.last_name)
            );
            if(account.username != null) {
                profile_id.setText(String.format("@%s", account.username));
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    protected void onDestroy() {
        app.xmpp.stopService();
        unregisterReceiver(jwReceiver);
        super.onDestroy();
    }

    @Override
    public void handleOnBackPressed() {
        super.handleOnBackPressed();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}