package dev.tinelix.jabwave.core.activities.example;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.mediaparkpk.base58android.Base58;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.MainActivity;
import dev.tinelix.jabwave.core.fragments.auth.AuthCloudPasswordFragment;
import dev.tinelix.jabwave.core.fragments.auth.AuthFragment;
import dev.tinelix.jabwave.core.fragments.auth.AuthProgressFragment;
import dev.tinelix.jabwave.core.fragments.auth.AuthTwoFactorFragment;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.api.base.SecureStorage;
import dev.tinelix.jabwave.ui.enums.HandlerMessages;
import dev.tinelix.jabwave.ui.views.base.XConstraintLayout;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;

/** <b>AuthActivity</b> - it is app wizard screen class with sign-in forms
 *  (username, password and etc.)
 *  <br>
 *  <br>Use this example for creating custom Network API client compatible with base Jabwave API.
 **/

public class AuthActivity extends AppCompatActivity {
    public Handler handler;
    private FragmentTransaction ft;
    private XConstraintLayout auth_layout;
    private String server;
    private String username;
    private String password;
    private Fragment fragment;
    private SharedPreferences xmpp_prefs;
    private SharedPreferences telegram_prefs;
    private SharedPreferences global_prefs;
    private JabwaveReceiver jwReceiver;

    private ClientService service;
    private final ServiceConnection clientConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ClientService.ClientServiceBinder binder = (ClientService.ClientServiceBinder) service;
            AuthActivity.this.service = binder.getService();
            ((JabwaveApp) AuthActivity.this.getApplicationContext()).clientService =
                    ((ClientService.ClientServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            AuthActivity.this.service.stopSelf();
            AuthActivity.this.service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.ApplicationTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        auth_layout = findViewById(R.id.auth_layout);
        auth_layout.setOnKeyboardStateListener(state -> {
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
        });
        fragment = new AuthFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, fragment);
        ft.commit();
        registerBroadcastReceiver();
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        xmpp_prefs = getSharedPreferences("xmpp", 0);
        telegram_prefs = getSharedPreferences("telegram", 0);
    }

    public void registerBroadcastReceiver() {
        jwReceiver = new JabwaveReceiver(this);
        registerReceiver(jwReceiver, new IntentFilter("dev.tinelix.jabwave.XMPP_RECEIVE"));
        registerReceiver(jwReceiver, new IntentFilter("dev.tinelix.jabwave.TELEGRAM_RECEIVE"));
    }

    public void unregisterBroadcastReceiver() {
        unregisterReceiver(jwReceiver);
    }

    /** Authorization function from sign-in screen, sends data to authorization server.

     @param username Username, phone number or another string
     @param password Password
     **/

    public void signIn(String username, String password) {
        HashMap<String, String> credentials;
        String[] username_mask = username.split("@");
        if (username_mask.length == 2) {
            this.username = username_mask[0];
            this.server = username_mask[1];
        }
        this.password = password;
        credentials = new SecureStorage().createCredentialsMap(
                this.username, this.server, this.password
        );
        service = new ClientService("undefined");
        service.start(this, clientConnection, credentials);

        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, new AuthProgressFragment());
        ft.commit();
    }

    /** Handling UI/Network message received from broadcast receiver.

        @param message Message code
        @param data Additional message information
     **/
    public void receiveState(int message, Bundle data) {
        if(message == HandlerMessages.AUTHORIZED) {
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
        } else if(message == HandlerMessages.NO_INTERNET_CONNECTION
                || message == HandlerMessages.AUTHENTICATION_ERROR) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, fragment);
            if(fragment instanceof AuthFragment) {
                ((AuthFragment) fragment).setAuthorizationData(server, username, password);
            }
            ft.commit();
            showSnackBar(message);
        } else if(message == HandlerMessages.REQUIRED_AUTH_CODE) {
            fragment = new AuthTwoFactorFragment();
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commit();
        } else if(message == HandlerMessages.REQUIRED_CLOUD_PASSWORD) {
            fragment = new AuthCloudPasswordFragment();
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commit();
        }
    }

    private void showSnackBar(int message) {
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, fragment);
        if(!(fragment instanceof AuthFragment)) {
            fragment = new AuthFragment();
        }
        ((AuthFragment) fragment).setAuthorizationData(server, username, password);
        ft.commit();
        Snackbar snackbar;
        int error_string_id = R.string.auth_error_network;
        if(message == HandlerMessages.AUTHENTICATION_ERROR) {
            error_string_id = R.string.invalid_jid_or_passw;
            snackbar = Snackbar.make(auth_layout, error_string_id, Snackbar.LENGTH_LONG);
        } else {
            snackbar = Snackbar
                    .make(auth_layout, error_string_id, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_btn, v -> signIn(username, password));
        }
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
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        if(service != null) {
            if (service.isConnected()) {
                service.stopSelf();
            }
        }
        super.onDestroy();
    }
}
