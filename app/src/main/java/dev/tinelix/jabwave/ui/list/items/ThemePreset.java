package dev.tinelix.jabwave.ui.list.items;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import dev.tinelix.jabwave.core.utilities.ThemePresets;

public class ThemePreset {
    public final long id;
    protected final String name;
    protected int actionBarColor;
    protected int msgrBackgroundColor;
    protected int inMessageBubbleColor;
    protected int outMessageBubbleColor;
    protected int inMessageTextColor;
    protected int outMessageTextColor;
    protected int appThemeBackgroundColor;
    protected int accentColor;
    protected int primaryTextColor;
    protected int secondaryTextColor;
    public int styleId;

    public ThemePreset(long id, String name) {
        this.styleId = styleId;
        this.id = id;
        this.name = name;
    }

    public ThemePreset setActionBarColor(int color) {
        this.actionBarColor = color;
        return this;
    }

    public ThemePreset setMessengerBackgroundColor(int color) {
        this.msgrBackgroundColor = color;
        return this;
    }

    public ThemePreset setIncomingMessageBubbleColor(int color) {
        this.inMessageBubbleColor = color;
        return this;
    }

    public ThemePreset setOutcomingMessageBubbleColor(int color) {
        this.outMessageBubbleColor = color;
        return this;
    }

    public ThemePreset setIncomingMessageTextColor(int color) {
        this.inMessageTextColor = color;
        return this;
    }

    public ThemePreset setOutcomingMessageTextColor(int color) {
        this.outMessageTextColor = color;
        return this;
    }

    public ThemePreset setAppThemeBackgroundColor(int color) {
        this.appThemeBackgroundColor = color;
        return this;
    }

    public ThemePreset setAccentColor(int color) {
        this.accentColor = color;
        return this;
    }

    public ThemePreset setPrimaryTextColor(int color) {
        this.primaryTextColor = color;
        return this;
    }

    public ThemePreset setSecondaryTextColor(int color) {
        this.secondaryTextColor = color;
        return this;
    }

    public String getName() {
        return name;
    }

    public int getActionBarColor() {
        return actionBarColor;
    }

    public int getMessengerBackgroundColor() {
        return msgrBackgroundColor;
    }

    public int getInMessageBubbleColor() {
        return inMessageBubbleColor;
    }

    public int getOutMessageBubbleColor() {
        return outMessageBubbleColor;
    }

    public int getAppThemeBackgroundColor() {
        return appThemeBackgroundColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getPrimaryTextColor() {
        return primaryTextColor;
    }

    public int getSecondaryTextColor() {
        return secondaryTextColor;
    }

    public void saveThemePreset(Context ctx) {
        SharedPreferences preset_prefs = ThemePresets.getPreferences(ctx, id);
        SharedPreferences.Editor editor = null;
        if(!preset_prefs.contains("name")) {
            editor = preset_prefs.edit();
            editor.putString("name", name);
            editor.putInt("style_id", styleId);
            editor.apply();
        }
        SharedPreferences app_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        editor = app_prefs.edit();
        editor.putLong("currentThemeId", id);
        editor.apply();
    }
}
