package dev.tinelix.jabwave.net.base.api.entities;


import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;

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
