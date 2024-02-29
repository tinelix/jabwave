package dev.tinelix.jabwave.core.ui.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.list.adapters.ContactsAdapter;
import dev.tinelix.jabwave.xmpp.api.entities.Contact;
import dev.tinelix.jabwave.core.list.sections.ContactsGroupSection;
import dev.tinelix.jabwave.core.ui.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.xmpp.enumerations.HandlerMessages;
import dev.tinelix.jabwave.xmpp.receivers.JabwaveReceiver;

public class AppActivity extends JabwaveActivity {

    private static AppActivity inst;
    private ContactsAdapter contactsAdapter;

    public static AppActivity getInstance() {
        return inst;
    }

    private AppBarConfiguration appBarConfiguration;
    private ArrayList<Contact> contacts;
    private ArrayList<Contact> groups;
    private ContactsGroupSection entityGroupSection;
    private LinearLayoutManager llm;
    private JabwaveReceiver jwReceiver;
    private JabwaveApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        app = ((JabwaveApp) getApplicationContext());
        findViewById(R.id.entityview).setVisibility(View.GONE);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        registerBroadcastReceiver();
        if(!app.xmpp.isConnected()) {
            connect();
        } else {
            getContacts();
        }
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
        contacts = app.xmpp.getContacts();
        groups = app.xmpp.getChatGroups();
        createEntityListAdapter();
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

    public void createEntityListAdapter() {
        contactsAdapter = new ContactsAdapter();
        if(groups.size() > 0) {
            for (Contact group : groups) {
                ArrayList<Contact> groupContacts = new ArrayList<>();
                for (Contact contact : contacts) {
                    if (contact.groups.contains(group.title)) {
                        groupContacts.add(contact);
                    }
                }
                entityGroupSection = new ContactsGroupSection(group, groupContacts, contactsAdapter);
                contactsAdapter.addSection(entityGroupSection);
            }
        } else {
            Contact group = new Contact(getResources().getString(R.string.general_category));
            entityGroupSection = new ContactsGroupSection(group, contacts, contactsAdapter);
            contactsAdapter.addSection(entityGroupSection);
        }

        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        ((RecyclerView) findViewById(R.id.entityview)).setLayoutManager(llm);
        ((RecyclerView) findViewById(R.id.entityview)).setAdapter(contactsAdapter);
        findViewById(R.id.entityview).setVisibility(View.VISIBLE);
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

}