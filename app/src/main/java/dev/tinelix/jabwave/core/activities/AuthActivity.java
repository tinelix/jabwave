package dev.tinelix.jabwave.core.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.mediaparkpk.base58android.Base58;

import java.nio.charset.StandardCharsets;
import java.util.List;

import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.fragments.auth.AuthFragment;
import dev.tinelix.jabwave.core.fragments.auth.AuthProgressFragment;
import dev.tinelix.jabwave.core.ui.views.base.XConstraintLayout;
import dev.tinelix.jabwave.core.listeners.OnKeyboardStateListener;
import dev.tinelix.jabwave.xmpp.enumerations.HandlerMessages;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;

public class AuthActivity extends AppCompatActivity {
    public Handler handler;
    private FragmentTransaction ft;
    private XConstraintLayout auth_layout;
    private String server;
    private String username;
    private String password;
    private AuthFragment authFragment;
    private SharedPreferences xmpp_prefs;
    private SharedPreferences telegram_prefs;
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
                FrameLayout frame = findViewById(R.id.fragment);
                Rect r = new Rect();
                View view = getWindow().getDecorView();
                view.getWindowVisibleDisplayFrame(r);
                LinearLayout.LayoutParams lp =
                        ((LinearLayout.LayoutParams) frame.getLayoutParams());
                if (state) {
                    lp.height = r.height();
                    lp.gravity = Gravity.TOP;
                    app_title.setVisibility(View.GONE);
                } else {
                    lp.height = LinearLayout.LayoutParams.MATCH_PARENT;
                    lp.gravity = Gravity.CENTER;
                    app_title.setVisibility(View.VISIBLE);
                }
                frame.setLayoutParams(lp);
            }
        });
        authFragment = new AuthFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, authFragment);
        ft.commit();
        registerBroadcastReceiver();
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        xmpp_prefs = getSharedPreferences("xmpp", 0);
        telegram_prefs = getSharedPreferences("telegram", 0);
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
        if(global_prefs.getString("network_type", "").equals("telegram")) {
            this.username = username;
            ((JabwaveApp) getApplication()).telegram.start(this, username);
        } else {
            String[] username_mask = username.split("@");
            if (username_mask.length == 2) {
                this.username = username_mask[0];
                this.server = username_mask[1];
            }
            this.password = password;
            ((JabwaveApp) getApplication())
                    .xmpp.start(AuthActivity.this, server, this.username, password);
        }

        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, new AuthProgressFragment());
        ft.commit();
    }

    public void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, authFragment);
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
            SharedPreferences.Editor editor;
            if(global_prefs.getString("network_type", "").equals("telegram")) {
                editor = telegram_prefs.edit();
                editor.putString("phone_number", username);
            } else {
                editor = xmpp_prefs.edit();
                editor.putString("server", server);
                editor.putString("username", username);
                editor.putString("password_hash",
                        Base58.encode(password.getBytes(StandardCharsets.UTF_8)));
            }
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, authFragment);
            authFragment.setAuthorizationData(server, username, password);
            ft.commit();
            Snackbar snackbar = Snackbar.make(auth_layout,
                    R.string.auth_error_network,
                    Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.retry_btn, view -> signIn(username, password));
            Log.d("ConnectionState", "State: " + ((JabwaveApp) getApplicationContext()).xmpp.getStatus());
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
