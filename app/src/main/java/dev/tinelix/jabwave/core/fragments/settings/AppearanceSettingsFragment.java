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
import dev.tinelix.jabwave.core.activities.SettingsActivity;
import dev.tinelix.jabwave.ui.list.adapters.ThemePresetsAdapter;
import dev.tinelix.jabwave.ui.list.items.ThemePreset;


public class AppearanceSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_appearance, null);
        listenPreferences();
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
                        ThemePreset preset = new ThemePreset(-4, getResources().getString(R.string._default));
                        Global.generateDefaultThemePreset(getContext(), preset);
                        presets.add(preset);
                        preset = new ThemePreset(-3, "Tinelix Design 2022");
                        Global.generateDefaultThemePreset(getContext(), preset);
                        presets.add(preset);
                        presets_view.setAdapter(new ThemePresetsAdapter(getContext(), presets));
                    }
                }
            }
        };
    }
}
