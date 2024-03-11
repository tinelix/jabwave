package dev.tinelix.jabwave.api.base.entities;


import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

public class ChatSender {
    public int type;
    public Object id;
    protected BaseClient client;
    public String name;
    public String first_name;
    public String last_name;

    public ChatSender(BaseClient client, Object id, int type) {
        this.client = client;
        this.id = id;
        /*
            Chat Sender Type available values:
            0 - Sent by user
            1 - Sent by anonymous user (chat)
         */
        this.type = type;
    }

    public void getChatSender() {}

    public void getChatSender(OnClientAPIResultListener listener) {}
}
