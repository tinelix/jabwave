package dev.tinelix.jabwave.net.base.api.entities;

import java.util.ArrayList;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;

public class Chat {
    // Base Chat Class used in Contacts list (AppActivity)
    public int type;
    public int network_type;
    public String title;
    public Object id;
    public ArrayList<String> groups;
    public int status;
    public byte[] photo;
    public ArrayList<Message> messages;

    public Chat(String title, int network_type) {
        /*
            Network Type available values:
            0 - XMPP
            1 - Telegram
         */
        this.network_type = network_type;
        this.title = title;
        this.groups = new ArrayList<>();
    }

    public Chat(Object id, String title, int network_type) {
        /*
            Network Type available values:
            0 - XMPP
            1 - Telegram
         */
        this.network_type = network_type;
        this.title = title;
        this.id = id;
        this.groups = new ArrayList<>();
    }

    public Chat(Object id, String title, int type, int network_type) {
        /*
            Network Type available values:
            0 - XMPP
            1 - Telegram
         */
        this.network_type = network_type;
        /*
            Chat Type available values:
            0 - Private chat
            1 - Secret chat
            2 - Public chat
            3 - Supergroup (Channel)
         */
        this.type = type;
        this.title = title;
        this.id = id;
        this.groups = new ArrayList<>();
    }

    public void loadMessages(BaseClient client) {

    }

    public void loadMessages(BaseClient client, OnClientAPIResultListener listener) {

    }

    public void sendMessage(BaseClient client, String text) {

    }

    public void sendMessage(BaseClient client, String text, OnClientAPIResultListener listener) {

    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public Message searchMessageById(long id) {
        for(Message message : messages) {
            if(message.id == id) {
                return message;
            }
        }
        return null;
    }

    public int getMessageIndexById(long id) {
        for(int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if(message.id == id) {
                return i;
            }
        }
        return -1;
    }

    public int getMessageIndexByFileId(int file_id) {
        for(int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if(message.getAttachmentIndex(file_id) > -1) {
                return i;
            }
        }
        return -1;
    }
}
