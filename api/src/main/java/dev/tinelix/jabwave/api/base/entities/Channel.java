package dev.tinelix.jabwave.api.base.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

/**
 * Chat class used in contacts list.
 */

public class Channel extends Chat {
    // Base Channel Class used in Contacts list (AppActivity)
    public int type;
    public ArrayList<String> groups;
    public int status;
    public byte[] photo;
    public ArrayList<Message> messages;
    protected long subscribers_count;
    /**
     * Default constructor for Channel class.
     * @param title Channel name
     * @param network_type Network type (for compatibility with String/Integer/Long IDs)
     */
    public Channel(String title, int network_type) {
        super(title, network_type);
        /*
            Network Type available values:
            0 - XMPP
            1 - Telegram
         */
        this.network_type = network_type;
        this.title = title;
        this.groups = new ArrayList<>();
    }

    /**
     * Default constructor for Channel class.
     * @param id String/Integer/Long ID
     * @param title Chat name
     * @param network_type Network type (for compatibility with String/Integer/Long IDs)
     */

    public Channel(Object id, String title, int network_type) {
        super(id, title, network_type);
        this.groups = new ArrayList<>();
    }

    public Channel(Object id, String title, int type, int network_type) {
        super(id, title, 3, network_type);
        this.groups = new ArrayList<>();
    }

    @Override
    public void sendMessage(BaseClient client, String text) {
        broadcast(client, text);
    }

    @Override
    public void sendMessage(BaseClient client, String text, OnClientAPIResultListener listener) {
        broadcast(client, text, listener);
    }

    @Override
    public Message searchMessageById(long id) {
        return searchPostsById(id);
    }

    @Override
    public void loadMessages(BaseClient client) {
        loadPosts(client);
    }

    @Override
    public void loadMessages(BaseClient client, OnClientAPIResultListener listener) {
        loadPosts(client, listener);
    }

    @Override
    public int getMessageIndexById(long id) {
        return getPostIndexById(id);
    }

    @Override
    public int getMessageIndexByFileId(int file_id) {
        return getPostIndexByFileId(file_id);
    }

    @Override
    public ArrayList<Message> getMessages() {
        return getPosts();
    }

    public void loadPosts(BaseClient client) {

    }

    public void loadPosts(BaseClient client, OnClientAPIResultListener listener) {

    }

    public void broadcast(BaseClient client, String text) {

    }

    public void broadcast(BaseClient client, String text, OnClientAPIResultListener listener) {

    }

    public ArrayList<Message> getPosts() {
        return messages;
    }

    public Message searchPostsById(long id) {
        for(Message message : messages) {
            if(message.id == id) {
                return message;
            }
        }
        return null;
    }

    public int getPostIndexById(long id) {
        for(int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if(message.id == id) {
                return i;
            }
        }
        return -1;
    }

    public int getPostIndexByFileId(int file_id) {
        for(int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if(message.getAttachmentIndex(file_id) > -1) {
                return i;
            }
        }
        return -1;
    }

    public long getSubscribersCount() {
        return subscribers_count;
    }

    public void getSubscribersCount(OnClientAPIResultListener listener) {

    }
}
