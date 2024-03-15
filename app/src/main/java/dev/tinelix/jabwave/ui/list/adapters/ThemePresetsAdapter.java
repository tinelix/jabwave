package dev.tinelix.jabwave.ui.list.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.SettingsActivity;
import dev.tinelix.jabwave.core.utilities.ThemePresets;
import dev.tinelix.jabwave.ui.list.items.ThemePreset;

public class ThemePresetsAdapter extends RecyclerView.Adapter<ThemePresetsAdapter.Holder> {

    private final Context ctx;
    private final ArrayList<ThemePreset> presets;
    private final SharedPreferences app_prefs;
    private RadioButton lastCheckedButton = null;

    public ThemePresetsAdapter(Context context, ArrayList<ThemePreset> presets) {
        this.ctx = context;
        this.presets = presets;
        app_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @NonNull
    @Override
    public ThemePresetsAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = View.inflate(ctx, R.layout.list_item_theme_preset, null);
        RecyclerView.LayoutParams lp =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );
        view.setLayoutParams(lp);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemePresetsAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        private final View view;
        private final View actionBar;
        private final View messengerView;
        private final CardView inMsgBubble;
        private final CardView outMsgBubble;
        private final View presetTitleLayout;
        private final TextView presetName;
        private final RadioButton button;

        public Holder(@NonNull View view) {
            super(view);
            this.view = view;
            this.actionBar = view.findViewById(R.id.actionbar_imitation);
            this.messengerView = view.findViewById(R.id.messenger_view);
            this.inMsgBubble = view.findViewById(R.id.incoming_bubble);
            this.outMsgBubble = view.findViewById(R.id.outcoming_bubble);
            this.presetTitleLayout = view.findViewById(R.id.preset_title_layout);
            this.presetName = view.findViewById(R.id.preset_name);
            this.button = view.findViewById(R.id.radio_btn);
        }

        public void bind(int position) {
            view.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            ThemePreset preset = presets.get(position);
            presetName.setText(preset.getName());
            actionBar.setBackgroundColor(preset.getActionBarColor());
            messengerView.setBackgroundColor(preset.getMessengerBackgroundColor());
            inMsgBubble.setCardBackgroundColor(preset.getInMessageBubbleColor());
            outMsgBubble.setCardBackgroundColor(preset.getOutMessageBubbleColor());
            presetTitleLayout.setBackgroundColor(preset.getAppThemeBackgroundColor());
            presetName.setTextColor(preset.getAccentColor());
            presetName.setSelected(true);
            button.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (lastCheckedButton != null) {
                    lastCheckedButton.setChecked(false);
                }
                lastCheckedButton = (RadioButton) button;
                ThemePresets.getPreferences(ctx, preset.id);
                preset.saveThemePreset(ctx);
                restartActivity(ctx);
            });
            if(app_prefs.getLong("currentThemeId", 0) == preset.id) {
                button.setChecked(true);
            }
        }

        private void restartActivity(Context ctx) {
            if(ctx instanceof SettingsActivity activity) {
                activity.restart();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
