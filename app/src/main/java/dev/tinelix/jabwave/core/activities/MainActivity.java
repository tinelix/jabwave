package dev.tinelix.jabwave.core.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.navigation.ui.AppBarConfiguration;


import android.view.Menu;
import android.view.MenuItem;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.core.activities.AppActivity;
import dev.tinelix.jabwave.core.activities.AuthActivity;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;

public class MainActivity extends JabwaveActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JabwaveApp app = ((JabwaveApp) getApplicationContext());
        if(!app.isAuthorized()) {
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