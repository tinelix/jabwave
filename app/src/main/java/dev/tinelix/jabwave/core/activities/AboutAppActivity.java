package dev.tinelix.jabwave.core.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import dev.tinelix.jabwave.BuildConfig;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.ui.views.base.JabwaveActionBar;

public class AboutAppActivity extends JabwaveActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        setActionBar();
        loadAppInfo();
    }

    private void setActionBar() {
        JabwaveActionBar actionbar = findViewById(R.id.actionbar);
        actionbar.setNavigationIconTint(R.color.white);
        setSupportActionBar(actionbar);
    }

    private void loadAppInfo() {
        TextView app_name = findViewById(R.id.app_name);
        TextView app_version = findViewById(R.id.app_version);
        TextView app_license_notif = findViewById(R.id.license_notification);

        app_name.setText(getResources().getString(R.string.app_name));
        app_version.setText(
                getResources().getString(
                        R.string.about_app_version,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.GIT_COMMIT
                )
        );
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            app_license_notif.setText(
                    Html.fromHtml(
                            getResources().getString(R.string.about_app_license),
                            Html.FROM_HTML_MODE_COMPACT
                    )
            );
        } else {
            app_license_notif.setText(
                    Html.fromHtml(getResources().getString(R.string.about_app_license))
            );
        }
    }
}
