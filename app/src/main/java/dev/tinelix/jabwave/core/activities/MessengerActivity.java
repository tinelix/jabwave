package dev.tinelix.jabwave.core.activities;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.net.base.api.entities.Chat;
import dev.tinelix.jabwave.net.base.api.entities.Message;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.ui.list.adapters.MessagesAdapter;
import dev.tinelix.jabwave.ui.list.sections.MessagesSection;
import dev.tinelix.jabwave.ui.views.base.JabwaveActionBar;

public class MessengerActivity extends JabwaveActivity {
    private ActionBar actionBar;
    private ClientService service;
    private JabwaveApp app;
    private JabwaveReceiver jwReceiver;
    private Object chat_id;
    private ArrayList<Message> messages;
    private RecyclerView messages_list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        setActionBar();
        app = ((JabwaveApp) getApplicationContext());
        service = app.clientService;
        actionBar = getSupportActionBar();
        int network_type = getIntent().getIntExtra("network_type", 0);
        if(network_type == 0) {
            chat_id = getIntent().getStringExtra("chat_id");
            if (chat_id != null) {
                String chat_title = getIntent().getStringExtra("chat_title");
                if(actionBar != null) {
                    actionBar.setTitle(chat_title);
                }
            } else {
                finish();
            }
        } else {
            chat_id = getIntent().getLongExtra("chat_id", 0);
            if ((long) chat_id != 0) {
                String chat_title = getIntent().getStringExtra("chat_title");
                if(actionBar != null) {
                    actionBar.setTitle(chat_title);
                }
            } else {
                finish();
            }
        }
        loadChat();
    }

    private void loadChat() {
        if(app.getCurrentNetworkType().equals("telegram")) {
            service.getChats().loadChat(chat_id, new OnClientAPIResultListener() {
                @Override
                public boolean onSuccess(HashMap<String, Object> map) {
                    Chat chat = (Chat) map.get("chat");
                    if (chat != null) {
                        chat.loadMessages(service.getClient(),
                                new OnClientAPIResultListener() {
                                    @Override
                                    public boolean onSuccess(HashMap<String, Object> map) {
                                        messages = chat.getMessages();
                                        createMessagesAdapter();
                                        return true;
                                    }

                                    @Override
                                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                                        return false;
                                    }
                                }
                        );
                    }
                    return false;
                }

                @Override
                public boolean onFail(HashMap<String, Object> map, Throwable t) {
                    return false;
                }
            });
        } else {
            Chat chat = service.getChats().getChatById(chat_id);
            chat.loadMessages(service.getClient());
            messages = chat.getMessages();
            createMessagesAdapter();
        }
    }

    private void createMessagesAdapter() {
        messages_list = findViewById(R.id.messages_list);
        MessagesAdapter adapter = new MessagesAdapter();
        adapter.addSection(
                new MessagesSection(this, messages, adapter)
        );
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        messages_list.setLayoutManager(llm);
        messages_list.setAdapter(adapter);
    }

    private void setActionBar() {
        JabwaveActionBar actionbar = findViewById(R.id.actionbar);
        actionbar.setNavigationIconTint(R.color.white);
        setSupportActionBar(actionbar);
        actionbar.setNavigationOnClickListener(v -> handleOnBackPressed());
    }
}
