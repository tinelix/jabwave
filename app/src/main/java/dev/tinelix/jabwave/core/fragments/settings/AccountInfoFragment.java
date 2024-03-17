package dev.tinelix.jabwave.core.fragments.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.api.base.entities.Account;
import dev.tinelix.jabwave.api.base.entities.Authenticator;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.core.activities.SettingsActivity;
import dev.tinelix.jabwave.core.services.base.ClientService;

public class AccountInfoFragment extends PreferenceFragmentCompat {
    private ClientService service;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_account_info, null);
        service = ((JabwaveApp) requireContext().getApplicationContext()).clientService;
        loadAccountInfo();
        if(getActivity() instanceof SettingsActivity activity) {
            Objects.requireNonNull(activity.getSupportActionBar())
                    .setTitle(getResources().getString(R.string.about_account));
        }
    }

    private void loadAccountInfo() {
        Preference fn_name_pref = findPreference("fl_name");
        Preference username_pref = findPreference("username");
        Preference password_pref = findPreference("password");
        Preference email_pref = findPreference("email");
        Preference phone_number = findPreference("phone_number");
        int authType = service.getAuthenticator().getType();
        Account account = service.getAccount();
        if(fn_name_pref != null && username_pref != null && email_pref != null && phone_number != null) {
            fn_name_pref.setSummary(String.format("%s %s", account.first_name, account.last_name));
            username_pref.setSummary(account.username);
            switch (authType) {
                case Authenticator.TYPE_REQUIRES_EMAIL,
                        Authenticator.TYPE_STEP_BY_STEP | Authenticator.TYPE_REQUIRES_EMAIL -> {
                    phone_number.setVisible(false);
                    email_pref.setSummary(account.getEmail());
                }
                case Authenticator.TYPE_REQUIRES_PHONE_NUMBER,
                        Authenticator.TYPE_STEP_BY_STEP | Authenticator.TYPE_REQUIRES_PHONE_NUMBER -> {
                    email_pref.setVisible(false);
                    phone_number.setSummary(String.format("+%s", account.getPhoneNumber()));
                }
                case Authenticator.TYPE_REQUIRES_EMAIL | Authenticator.TYPE_REQUIRES_PHONE_NUMBER -> {
                    email_pref.setVisible(true);
                    phone_number.setVisible(true);
                }
                case 0 -> {
                    email_pref.setVisible(false);
                    phone_number.setVisible(false);
                }
            }
        }
        if(password_pref != null) {
            password_pref.setOnPreferenceClickListener(preference -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(System.currentTimeMillis()));
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                switch (account.getPasswordType()) {
                    case Account.PASSWORD_TYPE_RESET_AWAIT_7_DAYS ->
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth + 7);
                    case Account.PASSWORD_TYPE_RESET_AWAIT_14_DAYS ->
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth + 14);
                    case Account.PASSWORD_TYPE_RESET_AWAIT_28_DAYS ->
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth + 28);
                }
                if(account.getPasswordType() > 0 &&
                        account.getPasswordState() == Account.PASSWORD_STATE_ACTIVE) {
                    AlertDialog dialog = new AlertDialog
                            .Builder(requireActivity())
                            .setTitle(R.string.change_account_data_title)
                            .setMessage(
                                    getResources().getString(
                                            R.string.reset_password_wait,
                                            Global.formatDateTime(
                                                    "dd MMMM",
                                                    calendar.getTime()
                                            )
                                    )
                            )
                            .setPositiveButton(
                                    android.R.string.ok, (dialog1, which) -> {
                                        resetPassword(calendar, account);
                                    }
                            )
                            .setNegativeButton(android.R.string.cancel, null)
                            .create();
                    dialog.show();
                } else {
                    resetPassword(calendar, account);
                }
                return false;
            });
        }
    }

    private void resetPassword(Calendar calendar, Account account) {
        View view = View.inflate(getActivity(), R.layout.dialog_change_account_password, null);
        TextInputEditText editor = view.findViewById(R.id.old_data);
        TextInputEditText editor_2 = view.findViewById(R.id.new_data);

        if(account.getPasswordType() > 0) {
            switch (account.getPasswordState()) {
                case Account.PASSWORD_STATE_ACTIVE ->
                        account.resetPassword("", new OnClientAPIResultListener() {
                            @Override
                            public boolean onSuccess(HashMap<String, Object> map) {
                                resetPassword(calendar, account);
                                return false;
                            }

                            @Override
                            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                                resetPassword(calendar, account);
                                return false;
                            }
                        });
                case Account.PASSWORD_STATE_RESET_AWAITING ->
                        Toast.makeText(
                                getActivity(),
                                getResources().getString(
                                        R.string.reset_password_wait_2,
                                        Global.formatDateTime("dd MMMM", calendar.getTime())
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                case Account.PASSWORD_STATE_RESET -> {
                    AlertDialog dialog = new AlertDialog
                            .Builder(requireActivity())
                            .setTitle(R.string.change_account_data_title)
                            .setView(view)
                            .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                                if (!Objects.requireNonNull(editor_2.getText())
                                        .toString()
                                        .equals(Objects.requireNonNull(editor.getText()).toString())) {
                                    editor.setError(getResources().getString(R.string.reset_password_do_not_match));
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create();
                    dialog.show();
                }
            }
        } else {
            AlertDialog dialog = new AlertDialog
                    .Builder(requireActivity())
                    .setTitle(R.string.change_account_data_title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                        if (!Objects.requireNonNull(editor_2.getText())
                                .toString()
                                .equals(Objects.requireNonNull(editor.getText()).toString())) {
                                    editor.setError(getResources().getString(R.string.reset_password_do_not_match));
                                }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            dialog.show();
        }
    }
}
