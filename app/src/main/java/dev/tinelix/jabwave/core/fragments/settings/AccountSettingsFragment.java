package dev.tinelix.jabwave.core.fragments.settings;

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.SettingsActivity;
import dev.tinelix.jabwave.core.utilities.FragmentNavigator;

public class AccountSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_account, null);
        listenPreferences();
    }

    private void listenPreferences() {
        Preference aboutAccountPref = findPreference("about_account");
        if(getActivity() instanceof SettingsActivity activity) {
            boolean isAuthenticated = activity.service.getAuthenticator().isAuthenticated();
            if (aboutAccountPref != null) {
                if(isAuthenticated) {
                    boolean isChangeable = activity.service.getAuthenticator().isChangeableAuthData();
                    String not_supported = getResources().getString(R.string.not_supported_by_protocol);
                    aboutAccountPref.setEnabled(isChangeable);
                    aboutAccountPref.setSummary(isChangeable ? null : not_supported);
                } else {
                    aboutAccountPref.setVisible(false);
                }
            }
        }
    }
}
