package dev.tinelix.jabwave.api.base.entities;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

/**
 * ChatSender - an object that stores information about the sender of a message.
 */

public class ChatSender {
    public int type;
    public Object id;
    protected BaseClient client;
    public String name;
    public String first_name;
    public String last_name;

    /**
     * Default constructor of ChatSender class.
     * @param client Network API client, usually extended from BaseClient if used specified API
     * @param id Chat sender ID
     * @param type Chat sender type
     * <br>
     * <br><b>Chat Sender Type available values:</b>
     * <br><code>0</code> - Sent by user
     * <br><code>1</code> - Sent by anonymous user or group chat
     */

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
