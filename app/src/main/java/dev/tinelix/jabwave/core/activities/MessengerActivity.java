package dev.tinelix.jabwave.core.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.ui.views.base.JabwaveActionBar;

public class MessengerActivity extends JabwaveActivity {
    private ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        setActionBar();
        actionBar = getSupportActionBar();
        int network_type = getIntent().getIntExtra("network_type", 0);
        if(network_type == 0) {
            String chat_id = getIntent().getStringExtra("chat_id");
            if (chat_id != null) {
                String chat_title = getIntent().getStringExtra("chat_title");
                if(actionBar != null) {
                    actionBar.setTitle(chat_title);
                }
            } else {
                finish();
            }
        } else {
            long chat_id = getIntent().getLongExtra("chat_id", 0);
            if (chat_id > 0) {
                String chat_title = getIntent().getStringExtra("chat_title");
                if(actionBar != null) {
                    actionBar.setTitle(chat_title);
                }
            } else {
                finish();
            }
        }
    }

    private void setActionBar() {
        JabwaveActionBar actionbar = findViewById(R.id.actionbar);
        actionbar.setNavigationIconTint(R.color.white);
        setSupportActionBar(actionbar);
    }
}
