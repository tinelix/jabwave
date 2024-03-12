package dev.tinelix.jabwave.core.activities;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.os.Bundle;
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
import dev.tinelix.jabwave.api.base.entities.SuperChat;
import dev.tinelix.jabwave.core.activities.base.JabwaveActivity;
import dev.tinelix.jabwave.core.receivers.JabwaveReceiver;
import dev.tinelix.jabwave.core.services.base.ClientService;
import dev.tinelix.jabwave.api.base.attachments.Attachment;
import dev.tinelix.jabwave.api.base.entities.Chat;
import dev.tinelix.jabwave.api.base.entities.Message;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
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
    private boolean isSuperChat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        setActionBar();
        app = ((JabwaveApp) getApplicationContext());
        service = app.clientService;
        actionBar = getSupportActionBar();
        int network_type = getIntent().getIntExtra("network_type", 0);
        isSuperChat = getIntent().getIntExtra("chat_type", 0) == 2;
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
        editor.setSendButtonListener(v -> chat.sendMessage(service.getClient(),
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
        ));
    }

    private void loadChat() {
        if(service.isAsyncAPIs()) {
            service.getChats().loadChat(chat_id, new OnClientAPIResultListener() {
                @Override
                public boolean onSuccess(HashMap<String, Object> map) {
                    chat = (Chat) map.get("chat");
                    if(isSuperChat) {
                        assert chat != null;
                        if(((SuperChat) chat).isRequiredAuth())
                            ((SuperChat) chat).join(service.getClient(), "tretdm-jabwave");
                    }
                    if (chat != null) {
                        MessageEditor editor = findViewById(R.id.message_editor);
                        editor.getEditorArea().setHint(
                                chat.type == 3 ?
                                        getResources().getString(R.string.broadcast) :
                                        getResources().getString(R.string.message)
                        );
                        chat.loadMessages(service.getClient(),
                                new OnClientAPIResultListener() {
                                    @SuppressLint("NotifyDataSetChanged")
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
            if(isSuperChat) {
                assert chat != null;
                if(((SuperChat) chat).isRequiredAuth())
                    ((SuperChat) chat).join(service.getClient(), "tretdm-jabwave");
            }
            chat.loadMessages(service.getClient());
            messages = chat.getMessages();
            createMessagesAdapter();
        }
    }

    private void createMessagesAdapter() {
        messages_list = findViewById(R.id.messages_list);
        adapter = new MessagesAdapter(this, messages, chat, service);
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
            case HandlerMessages.CHATS_LOADED:
                updateFileFromMessage(data);
                break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateFileFromMessage(Bundle data) {
        int file_id = data.getInt("file_id");
        int msg_pos = chat.getMessageIndexById(file_id);
        Message msg = chat.messages.get(msg_pos);
        int attach_pos = msg.getAttachmentIndex(file_id);
        Attachment attachment = msg.getAttachments().get(attach_pos);
        if(data.getBoolean("updateComplete")) {
            attachment.updateState(1);
            ArrayList<Attachment> attachments = msg.getAttachments();
            attachments.set(attach_pos, attachment);
            msg.setAttachments(attachments);
            chat.messages.set(msg_pos, msg);
            adapter.notifyDataSetChanged();
        }
    }
}
