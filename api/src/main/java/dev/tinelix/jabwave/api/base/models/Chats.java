package dev.tinelix.jabwave.api.base.models;

import java.util.ArrayList;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.base.entities.Chat;

public class Chats {
    protected final BaseClient client;
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

    public ArrayList<Chat> getChannels() {
        ArrayList<Chat> channels = new ArrayList<>();
        for (Chat chat : chats) {
            if(chat.type == 3) {
                channels.add(chat);
            }
        }
        return channels;
    }

    public ArrayList<Chat> getGroupChats() {
        ArrayList<Chat> group_chats = new ArrayList<>();
        for (Chat chat : chats) {
            if(chat.type == 2) {
                group_chats.add(chat);
            }
        }
        return group_chats;
    }

    public ArrayList<Chat> getPeople() {
        ArrayList<Chat> people = new ArrayList<>();
        for (Chat chat : chats) {
            if(chat.type <= 1 && chat.type >= 0) {
                people.add(chat);
            }
        }
        return people;
    }
}
