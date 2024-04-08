package dev.tinelix.jabwave.core.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import dev.tinelix.jabwave.BuildConfig;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.ui.views.base.JabwaveActionBar;

public class AboutActivity extends JabwaveActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        setActionBar();
        loadAppInfo();
    }

    private void setActionBar() {
        JabwaveActionBar actionbar = findViewById(R.id.actionbar);
        actionbar.setNavigationIconTint(Global.getColorAttribute(
                this, R.attr.actionBarTint
        ), false);
        setSupportActionBar(actionbar);
        actionbar.setNavigationOnClickListener(v -> handleOnBackPressed());
    }

    private void loadAppInfo() {
        TextView app_name = findViewById(R.id.app_name);
        TextView app_version = findViewById(R.id.app_version);
        TextView app_license_notif = findViewById(R.id.license_notification);
        Button source_code_link = findViewById(R.id.source_code_link);

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

        source_code_link.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.SOURCE_CODE));
            startActivity(intent);
        });
    }
}
