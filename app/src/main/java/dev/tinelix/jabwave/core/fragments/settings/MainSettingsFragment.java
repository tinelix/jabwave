package dev.tinelix.jabwave.core.fragments.settings;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.AppActivity;
import dev.tinelix.jabwave.core.activities.SettingsActivity;
import dev.tinelix.jabwave.core.utilities.FragmentNavigator;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.Objects;

public class MainSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_main, null);
        int colorAttr = android.R.attr.textColorSecondary;

        // Workaround for support customized daylight themes
        // from https://stackoverflow.com/a/52877871

        TypedArray ta = Objects.requireNonNull(getContext())
                .getTheme()
                .obtainStyledAttributes(new int[]{com.google.android.material.R.attr.colorAccent});
        int iconColor = ta.getColor(0, 0);
        ta.recycle();
        tintIcons(getPreferenceScreen(), iconColor);
        listenPreferences();
        if(getActivity() instanceof AppActivity activity) {
            if(activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(
                        getResources().getString(R.string.settings)
                );
            }
        }
    }

    private void listenPreferences() {
        Preference accountPref = findPreference("account");
        if (accountPref != null) {
            accountPref.setOnPreferenceClickListener(preference -> {
                showPreferenceInNewActivity(FragmentNavigator.FRAGMENT_ACCOUNT_SETTINGS);
                return false;
            });
        }
        Preference appearancePref = findPreference("appearance");
        if (appearancePref != null) {
            appearancePref.setOnPreferenceClickListener(preference -> {
                showPreferenceInNewActivity(FragmentNavigator.FRAGMENT_APPEARANCE_SETTINGS);
                return false;
            });
        }
    }

    private void showPreferenceInNewActivity(int fragment_id) {
        Intent intent = new Intent(getContext(), SettingsActivity.class);
        intent.putExtra("fragment_id", fragment_id);
        startActivity(intent);
    }

    private void tintIcons(Preference preference, int color) {
        if (preference instanceof PreferenceGroup group) {
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                tintIcons(group.getPreference(i), color);
            }
        } else {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    DrawableCompat.setTint(icon, color);
                } else {
                    icon.setColorFilter(
                            ContextCompat.getColor(Objects.requireNonNull(getContext()), color),
                            android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
            }
        }
    }
}
