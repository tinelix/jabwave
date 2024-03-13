package dev.tinelix.jabwave.core.fragments.settings;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import dev.tinelix.jabwave.R;
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
    }

    private static void tintIcons(Preference preference, int color) {
        if (preference instanceof PreferenceGroup group) {
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                tintIcons(group.getPreference(i), color);
            }
        } else {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                DrawableCompat.setTint(icon, color);
            }
        }
    }
}
