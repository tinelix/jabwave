package dev.tinelix.jabwave.user_interface.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.user_interface.list_adapters.EntityListAdapter;
import dev.tinelix.jabwave.user_interface.list_items.EntityList;
import dev.tinelix.jabwave.xmpp.enumerations.HandlerMessages;
import dev.tinelix.jabwave.xmpp.receivers.JabwaveReceiver;

public class AppActivity extends AppCompatActivity {

    private static AppActivity inst;
    public static AppActivity getInstance() {
        return inst;
    }

    private AppBarConfiguration appBarConfiguration;
    private ArrayList<EntityList> entityLists;
    private EntityListAdapter entityAdapter;
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
            getConversations();
        }
    }

    private void registerBroadcastReceiver() {
        jwReceiver = new JabwaveReceiver(this) {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                Bundle data = intent.getExtras();
                receiveState(data.getInt("msg"), data);
            }
        };
        registerReceiver(jwReceiver, new IntentFilter(
                "dev.tinelix.jabwave.XMPP_RECEIVE"));
    }

    private void connect() {
        if(!app.xmpp.isConnected()) {
            app.xmpp.start(this, app.getXmppPreferences().getString("server", ""),
                    app.getXmppPreferences().getString("username", ""), app.getXmppPreferences().getString("account_password", ""));
        }
    }

    private void getConversations() {
        entityLists = app.xmpp.getConversations();
        createEntityListAdapter();
    }

    private void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.AUTHORIZED) {
            Log.d("App", "Loading converstaions...");
            getConversations();
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
        entityAdapter = new EntityListAdapter(this, entityLists);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        ((RecyclerView) findViewById(R.id.entityview)).setLayoutManager(llm);
        ((RecyclerView) findViewById(R.id.entityview)).setAdapter(entityAdapter);
        findViewById(R.id.entityview).setVisibility(View.VISIBLE);
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
}