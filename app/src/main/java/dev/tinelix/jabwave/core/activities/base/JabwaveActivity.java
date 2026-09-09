package dev.tinelix.jabwave.core.activities.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowInsets;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
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

        int statusBarColorRes = R.color.statusBarColor;

        switch ((int) themeId) {
            case 2 -> statusBarColorRes = R.color.statusBarColorGreen;
            case 3 -> statusBarColorRes = R.color.statusBarColorRed;
            case 4 -> statusBarColorRes = R.color.statusBarColorViolet;
            case 5 -> statusBarColorRes = R.color.statusBarColorOrange;
            case 6 -> statusBarColorRes = R.color.statusBarColorTeal;
            case 7 -> statusBarColorRes = R.color.statusBarColorOcean;
            case 8 -> statusBarColorRes = R.color.statusBarColorNeon;
        }

        setStatusBar(getResources().getColor(statusBarColorRes));
    }

    private void setStatusBar(@ColorRes int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets insets) {
                    Insets statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars());
                    view.setBackgroundColor(statusBarColor);
                    view.setPadding(0, statusBarInsets.top, 0, 0);
                    return insets;
                }
            });
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(statusBarColor);
        }
    }

    protected void handleOnBackPressed() {
        finish();
    }
}
