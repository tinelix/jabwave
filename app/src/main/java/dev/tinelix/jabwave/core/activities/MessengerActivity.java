package dev.tinelix.jabwave.core.activities;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.net.base.api.entities.Chat;
import dev.tinelix.jabwave.net.base.api.entities.Message;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.ui.enums.HandlerMessages;
import dev.tinelix.jabwave.ui.list.adapters.MessagesAdapter;
import dev.tinelix.jabwave.ui.views.MessageEditor;
import dev.tinelix.jabwave.ui.views.base.JabwaveActionBar;

public class MessengerActivity extends JabwaveActivity {
    private ActionBar actionBar;
    private ClientService service;
    private JabwaveApp app;
    private JabwaveReceiver jwReceiver;
    private Object chat_id;
    private ArrayList<Message> messages;
    private RecyclerView messages_list;
    private Chat chat;
    private MessagesAdapter adapter;

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
        registerBroadcastReceiver();
        loadChat();
        setUiListeners();
    }

    public void registerBroadcastReceiver() {
        jwReceiver = new JabwaveReceiver(this);
        registerReceiver(jwReceiver, new IntentFilter("dev.tinelix.jabwave.XMPP_RECEIVE"));
        registerReceiver(jwReceiver, new IntentFilter("dev.tinelix.jabwave.TELEGRAM_RECEIVE"));
    }

    private void setUiListeners() {
        MessageEditor editor = findViewById(R.id.message_editor);
        editor.setSendButtonListener(v -> {
            if(service.isAsyncAPIs()) {
                chat.sendMessage(service.getClient(),
                        editor.getEditorArea().getText().toString(),
                        new OnClientAPIResultListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public boolean onSuccess(HashMap<String, Object> map) {
                                Global.triggerReceiverIntent(
                                        MessengerActivity.this,
                                        HandlerMessages.MESSAGE_SENT
                                );
                                return false;
                            }

                            @Override
                            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                                editor.getEditorArea().setText("");
                                Toast.makeText(
                                        MessengerActivity.this,
                                        getResources().getString(R.string.error),
                                        Toast.LENGTH_LONG
                                ).show();
                                return false;
                            }
                        }
                );
            } else {
                chat.sendMessage(service.getClient(), editor.getEditorArea().getText().toString());
            }
        });
    }

    private void loadChat() {
        if(service.isAsyncAPIs()) {
            service.getChats().loadChat(chat_id, new OnClientAPIResultListener() {
                @Override
                public boolean onSuccess(HashMap<String, Object> map) {
                    chat = (Chat) map.get("chat");
                    if (chat != null) {
                        MessageEditor editor = findViewById(R.id.message_editor);
                        editor.getEditorArea().setHint(
                                chat.type == 3 ?
                                        getResources().getString(R.string.broadcast) :
                                        getResources().getString(R.string.message)
                        );
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
            chat = service.getChats().getChatById(chat_id);
            chat.loadMessages(service.getClient());
            messages = chat.getMessages();
            createMessagesAdapter();
        }
    }

    private void createMessagesAdapter() {
        messages_list = findViewById(R.id.messages_list);
        adapter = new MessagesAdapter(this, messages, chat);
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

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        super.onDestroy();
    }

    private void unregisterBroadcastReceiver() {
        unregisterReceiver(jwReceiver);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void receiveState(int msg, Bundle data) {
        switch (msg) {
            case HandlerMessages.MESSAGE_SENT:
                MessageEditor editor = findViewById(R.id.message_editor);
                editor.getEditorArea().setText("");
                adapter.notifyDataSetChanged();
                break;
        }
    }
}
