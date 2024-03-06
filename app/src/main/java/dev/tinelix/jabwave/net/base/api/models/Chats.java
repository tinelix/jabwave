package dev.tinelix.jabwave.net.base.api.models;

import java.util.ArrayList;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.base.api.entities.Chat;

public class Chats {
    private final BaseClient client;
    private ArrayList<Chat> chats;
    private ArrayList<ChatGroup> groups;

    public Chats(BaseClient client) {
        this.client = client;
    }

    public void loadChats() {}

    public void loadChats(OnClientAPIResultListener listener) {}

    public Chat getChatById(Object id) {
        for (Chat chat : chats) {
            if(chat.id.equals(id)) {
                return chat;
            }
        }
        return null;
    }

    public ArrayList<Chat> getList() {
        return chats;
    }

    public ArrayList<ChatGroup> getGroupsList() {
        return groups;
    }

    public Chat loadChat(Object chat_id) {
        return null;
    }

    public void loadChat(Object chat_id, OnClientAPIResultListener listener) {

    }

    public int getChatIndex(Chat chat) {
        for(int i = 0; i < chats.size(); i++) {
            if(chats.get(i).equals(chat)) {
                return i;
            }
        }
        return -1;
    }
}
