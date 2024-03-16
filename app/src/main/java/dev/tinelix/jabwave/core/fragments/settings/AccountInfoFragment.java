package dev.tinelix.jabwave.core.fragments.settings;

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.api.base.entities.Account;
import dev.tinelix.jabwave.api.base.entities.Authenticator;
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
        Preference fn_name = findPreference("fl_name");
        Preference username_pref = findPreference("username");
        Preference email_pref = findPreference("email");
        Preference phone_number = findPreference("phone_number");
        int authType = service.getAuthenticator().getType();
        Account account = service.getAccount();
        if(fn_name != null && username_pref != null && email_pref != null && phone_number != null) {
            fn_name.setSummary(String.format("%s %s", account.first_name, account.last_name));
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
    }
}
