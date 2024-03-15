package dev.tinelix.jabwave.core.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.core.activities.base.JabwaveFragmentActivity;
import dev.tinelix.jabwave.core.fragments.settings.AppearanceSettingsFragment;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.core.utilities.FragmentNavigator;
import dev.tinelix.jabwave.ui.views.base.JabwaveActionBar;

public class SettingsActivity extends JabwaveActivity {
    private Fragment fragment;
    private JabwaveReceiver jwReceiver;
    private JabwaveApp app;
    public ClientService service;
    private boolean neededToRestartApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        setActionBar();
        app = ((JabwaveApp) getApplicationContext());
        service = app.clientService;
        if(getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            createSettingsFragment(extras.getInt("fragment_id"));
            neededToRestartApp = extras.getBoolean("needed_to_restart_app");
            registerBroadcastReceiver();
        } else {
            Log.e(JabwaveApp.APP_TAG, "Fragment ID not specified before SettingsActivity starts");
            finish();
        }
    }

    private void registerBroadcastReceiver() {
        jwReceiver = new JabwaveReceiver(this);
        registerReceiver(jwReceiver, new IntentFilter("dev.tinelix.jabwave.XMPP_RECEIVE"));
        registerReceiver(jwReceiver, new IntentFilter("dev.tinelix.jabwave.TELEGRAM_RECEIVE"));
    }

    private void setActionBar() {
        JabwaveActionBar actionbar = findViewById(R.id.actionbar);
        actionbar.setNavigationIconTint(R.color.white);
        setSupportActionBar(actionbar);
        actionbar.setNavigationOnClickListener(v -> handleOnBackPressed());
    }

    private void createSettingsFragment(int fragment_id) {
        fragment = FragmentNavigator.switchToAnotherFragment(
                getSupportFragmentManager(), R.id.app_fragment, fragment_id
        );
        if(fragment == null) {
            Log.e(JabwaveApp.APP_TAG, "Invalid Fragment ID before SettingsActivity starts");
            finish();
        }
    }

    // Restarting to restore activity to its normal state or to apply theme and font changes
    public void restart() {
        Intent intent = new Intent(this, getClass());
        if(fragment != null) {
            Bundle extras = getIntent().getExtras();
            intent.putExtra("fragment_id", extras.getInt("fragment_id"));
            intent.putExtra("needed_to_restart_app", true);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void handleOnBackPressed() {
        if(neededToRestartApp) {
            ((JabwaveApp) getApplication()).restart();
        }
        super.handleOnBackPressed();
    }
}
