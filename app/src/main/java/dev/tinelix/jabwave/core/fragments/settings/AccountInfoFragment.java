package dev.tinelix.jabwave.core.fragments.settings;

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.api.base.entities.Authenticator;
import dev.tinelix.jabwave.core.services.base.ClientService;

public class AccountInfoFragment extends PreferenceFragmentCompat {
    private ClientService service;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_account_info, null);
        service = ((JabwaveApp) requireContext().getApplicationContext()).clientService;
        loadAccountInfo();
    }

    private void loadAccountInfo() {
        Preference email_pref = findPreference("email");
        Preference phone_number = findPreference("phone_number");
        int authType = service.getAuthenticator().getType();
        if(email_pref != null && phone_number != null)
            switch (authType) {
                case Authenticator.TYPE_REQUIRES_EMAIL -> phone_number.setVisible(false);
                case Authenticator.TYPE_REQUIRES_PHONE_NUMBER -> email_pref.setVisible(false);
                case Authenticator.TYPE_REQUIRES_EMAIL | Authenticator.TYPE_REQUIRES_PHONE_NUMBER -> {
                    email_pref.setVisible(true);
                    phone_number.setVisible(true);
                }
            }
    }
}
