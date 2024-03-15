package dev.tinelix.jabwave.core.activities.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.utilities.ThemePresets;

public class JabwaveActivity extends AppCompatActivity {
    private SharedPreferences global_prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getOnBackPressedDispatcher().addCallback(
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        JabwaveActivity.this.handleOnBackPressed();
                    }
                }
        );

        global_prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkTheme = global_prefs.getBoolean("darkTheme", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        setThemePreset();
    }

    private void setThemePreset() {
        long themeId = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getLong("currentThemeId", 0);
        SharedPreferences presetPrefs = ThemePresets.getPreferences(this, themeId);
        int styleId = presetPrefs.getInt("style_id", 0);
        if(styleId != 0) {
            setTheme(styleId);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switch ((int) themeId) {
                case -7 -> setStatusBar(R.color.statusBarColor);
                case -6 -> setStatusBar(R.color.statusBarColorGreen);
                case -5 -> setStatusBar(R.color.statusBarColorRed);
                case -4 -> setStatusBar(R.color.statusBarColorViolet);
                case -3 -> setStatusBar(R.color.statusBarColorOrange);
                case -2 -> setStatusBar(R.color.statusBarColorTeal);
                case -1 -> setStatusBar(R.color.statusBarColorOcean);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBar(@ColorRes int statusBarColor) {
        getWindow().setStatusBarColor(getResources().getColor(statusBarColor));
    }

    protected void handleOnBackPressed() {
        finish();
    }
}
