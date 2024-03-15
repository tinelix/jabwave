package dev.tinelix.jabwave.core.fragments.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.AppActivity;
import dev.tinelix.jabwave.core.activities.SettingsActivity;
import dev.tinelix.jabwave.core.utilities.ThemePresets;
import dev.tinelix.jabwave.ui.list.adapters.ThemePresetsAdapter;
import dev.tinelix.jabwave.ui.list.items.ThemePreset;


public class AppearanceSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_appearance, null);
        listenPreferences();
        if(getActivity() instanceof SettingsActivity activity) {
            activity.getSupportActionBar().setTitle(
                    getResources().getString(R.string.appearance)
            );
        }
    }

    private void listenPreferences() {
        Preference presetsPref = findPreference("theme_presets");
    }

    @SuppressWarnings("rawtypes")
    @NonNull
    @Override
    protected RecyclerView.Adapter onCreateAdapter(@NonNull PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            @Override
            public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                View view = holder.itemView;
                Preference preference = getItem(position);
                if (preference != null) {
                    if (preference.getKey().equals("theme_presets_list")) {
                        RecyclerView presets_view = view.findViewById(R.id.presets_view);
                        ArrayList<ThemePreset> presets = new ArrayList<>();
                        loadThemePresets(presets);
                        presets_view.setAdapter(new ThemePresetsAdapter(getActivity(), presets));
                }
            }
        };
    };
}

    private void loadThemePresets(ArrayList<ThemePreset> presets) {
        for(int i = 0; i < 7; i++) {
            ThemePreset preset = new ThemePreset(-7 + i,
                    getResources().getStringArray(R.array.theme_presets)[i]
            );
            ThemePresets.generateThemePreset(getContext(), preset);
            presets.add(preset);
        }
    }
}
