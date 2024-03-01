package dev.tinelix.jabwave.core.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.ui.fragments.auth.AuthFragment;
import dev.tinelix.jabwave.core.ui.fragments.auth.AuthProgressFragment;
import dev.tinelix.jabwave.core.ui.views.base.XConstraintLayout;
import dev.tinelix.jabwave.core.ui.listeners.OnKeyboardStateListener;
import dev.tinelix.jabwave.telegram.enumerations.HandlerMessages;
import dev.tinelix.jabwave.telegram.receivers.JabwaveReceiver;

public class AuthActivity extends AppCompatActivity {
    public Handler handler;
    private FragmentTransaction ft;
    private XConstraintLayout auth_layout;
    private String server;
    private String username;
    private String password;
    private AuthFragment authFragment;
    private SharedPreferences xmpp_prefs;
    private SharedPreferences global_prefs;
    private JabwaveReceiver jwReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.ApplicationTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        auth_layout = findViewById(R.id.auth_layout);
        auth_layout.setOnKeyboardStateListener(new OnKeyboardStateListener() {
            @Override
            public void onKeyboardStateChanged(boolean state) {
                ConstraintLayout app_title = findViewById(R.id.app_title);
                if (state) {
                    app_title.setVisibility(View.GONE);
                } else {
                    app_title.setVisibility(View.VISIBLE);
                }
            }
        });
        authFragment = new AuthFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dynamic_fragment_layout, authFragment);
        ft.commit();
        registerBroadcastReceiver();
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        xmpp_prefs = getSharedPreferences("xmpp", 0);
    }

    public void registerBroadcastReceiver() {
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

    public void unregisterBroadcastReceiver() {
        unregisterReceiver(jwReceiver);
    }

    public void signIn(String username, String password) {
        String[] username_mask = username.split("@");
        if(username_mask.length == 2) {
            this.username = username_mask[0];
            this.server = username_mask[1];
        }
        this.password = password;
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.dynamic_fragment_layout, new AuthProgressFragment());
        ft.commit();
        ((JabwaveApp) getApplicationContext())
                .telegram.start(AuthActivity.this, server, this.username, password);
    }

    public void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(server, username, password);
            ft.commit();
            Snackbar snackbar = Snackbar.make(auth_layout,
                    R.string.auth_error_network,
                    Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.retry_btn, view -> signIn(username, password));
            View snackbarView = snackbar.getView();
            TextView snackTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            snackTextView.setMaxLines(3);
            snackTextView.setTextColor(getResources().getColor(R.color.black));
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setActionTextColor(getResources().getColor(R.color.accentColor));
            Button snackActionBtn = snackbarView.findViewById(com.google.android.material.R.id.snackbar_action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                snackActionBtn.setLetterSpacing(0);
            }
            snackbar.show();
        } else if(message == HandlerMessages.AUTHORIZED) {
            SharedPreferences.Editor editor = xmpp_prefs.edit();
            editor.putString("server", server);
            editor.putString("username", username);
            editor.putString("account_password", password);
            editor.putString("account_password_sha256", Global.generateSHA256Hash(password));
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.dynamic_fragment_layout, authFragment);
            authFragment.setAuthorizationData(server, username, password);
            ft.commit();
            Snackbar snackbar = Snackbar.make(auth_layout,
                    R.string.auth_error_network,
                    Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.retry_btn, view -> signIn(username, password));
            Log.d("ConnectionState", "State: " +
                    ((JabwaveApp) getApplicationContext()).telegram.getStatus()
            );
            View snackbarView = snackbar.getView();
            TextView snackTextView = snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_text
            );
            snackTextView.setMaxLines(3);
            snackTextView.setTextColor(getResources().getColor(R.color.black));
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setActionTextColor(getResources().getColor(R.color.accentColor));
            Button snackActionBtn = snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_action
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                snackActionBtn.setLetterSpacing(0);
            }
            snackbar.show();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        super.onDestroy();
    }
}
