package dev.tinelix.jabwave.core.fragments.settings;

import android.content.Intent;
import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.SettingsActivity;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.core.utilities.FragmentNavigator;

public class AccountSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_account, null);
        listenPreferences();
        if(getActivity() instanceof SettingsActivity activity) {
            activity.getSupportActionBar().setTitle(
                    getResources().getString(R.string.account)
            );
        }

        listenPreferences();
    }

    private void listenPreferences() {
        Preference aboutAccountPref = findPreference("about_account");
        if(getActivity() instanceof SettingsActivity activity) {
            ClientService service = activity.service;
            if(service != null && service.getAuthenticator() != null) {
                boolean isAuthenticated = service.getAuthenticator().isAuthenticated();
                if (aboutAccountPref != null) {
                    if (isAuthenticated) {
                        boolean isChangeable = activity.service.getAuthenticator().isChangeableAuthData();
                        String not_supported = getResources().getString(R.string.not_supported_by_protocol);
                        aboutAccountPref.setEnabled(isChangeable);
                        aboutAccountPref.setSummary(isChangeable ? null : not_supported);
                        aboutAccountPref.setOnPreferenceClickListener(preference -> {
                            showPreferenceInNewActivity();
                            return false;
                        });
                    } else {
                        aboutAccountPref.setVisible(false);
                    }
                }
            }
        }
    }

    private void showPreferenceInNewActivity() {
        Intent intent = new Intent(getContext(), SettingsActivity.class);
        intent.putExtra("fragment_id", FragmentNavigator.FRAGMENT_ACCOUNT_INFO);
        startActivity(intent);
    }
}
