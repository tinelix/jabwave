package dev.tinelix.jabwave.core.ui.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.list.adapters.ContactsAdapter;
import dev.tinelix.jabwave.core.ui.fragments.app.ContactsListFragment;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.core.list.sections.ContactsGroupSection;
import dev.tinelix.jabwave.core.ui.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.xmpp.api.entities.ContactsGroup;
import dev.tinelix.jabwave.xmpp.enumerations.HandlerMessages;
import dev.tinelix.jabwave.xmpp.receivers.JabwaveReceiver;

public class AppActivity extends JabwaveActivity {

    private static AppActivity inst;
    private ContactsAdapter contactsAdapter;
    private Fragment fragment;
    private FragmentTransaction ft;

    public static AppActivity getInstance() {
        return inst;
    }

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
            getContacts();
        }
        createMainFragment();
    }

    private void createMainFragment() {
        fragment = new ContactsListFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.app_fragment, fragment);
        ft.commit();
    }

    private void registerBroadcastReceiver() {
        jwReceiver = new JabwaveReceiver(this);
        registerReceiver(
                jwReceiver,
                new IntentFilter("dev.tinelix.jabwave.XMPP_RECEIVE")
        );
    }

    private void connect() {
        if(!app.xmpp.isConnected()) {
            app.xmpp.start(
                    this, app.getXmppPreferences().getString("server", ""),
                    app.getXmppPreferences().getString("username", ""),
                    app.getXmppPreferences().getString("account_password", "")
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
        if(message == HandlerMessages.AUTHORIZED) {
            Log.d(JabwaveApp.APP_TAG, "Loading contacts...");
            getContacts();
        } else if(message == HandlerMessages.ROSTER_CHANGED) {
            String jid = data.getString("presence_jid");
            Contact contact = contactsAdapter.searchByJid(jid);
            if(contact != null) {
                contact.custom_status = data.getString("presence_status");
                contactsAdapter.setByJid(jid.split("/")[0], contact);
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

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
            //return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        app.xmpp.stopService();
        unregisterReceiver(jwReceiver);
        super.onDestroy();
    }
}