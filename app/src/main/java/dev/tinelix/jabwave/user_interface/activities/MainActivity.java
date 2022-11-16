package dev.tinelix.jabwave.user_interface.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.user_interface.list_adapters.EntityListAdapter;
import dev.tinelix.jabwave.user_interface.list_items.EntityList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private JabwaveApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((JabwaveApp) getApplicationContext());
        if(app.getXmppPreferences().getString("server", "").length() == 0 ||
                app.getXmppPreferences().getString("username", "").length() == 0 ||
                app.getXmppPreferences().getString("account_password_sha256", "").length() == 0 ||
                app.getXmppPreferences().getString("account_password", "").length() == 0) {
            Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(getApplicationContext(), AppActivity.class);
            startActivity(intent);
            finish();
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
}