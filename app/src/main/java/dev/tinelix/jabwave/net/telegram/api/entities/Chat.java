package dev.tinelix.jabwave.net.telegram.api.entities;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.entities.Message;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;

public class Chat extends dev.tinelix.jabwave.net.base.api.entities.Chat {
    // Contact Class used in Contacts list (AppActivity)
    public int type;
    public String title;
    public long id;
    public ArrayList<String> groups;
    public int status;
    private VCard vCard;

    public Chat(String title) {
        super(title, 1);
        this.title = title;
    }

    public Chat(long id, String title, ArrayList<String> groups, int status) {
        super(id, title, 1);
        this.title = title;
        this.id = id;
        this.groups = groups;
        this.status = status;
    }

    public Chat(long id) {
        super(id, "(Unknown)", 1);
        this.id = id;
    }

    public VCard getVCard() {
        return vCard;
    }

    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }

    public void loadPhoto(byte[] bytes, File file) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(bytes);
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadMessages(BaseClient client, OnClientAPIResultListener listener) {
        client.send(new TdApi.GetChatHistory(id, 0, -25, 50, false),
                new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        if(map.get("result") instanceof TdApi.Messages) {
                            Log.d(JabwaveApp.APP_TAG, String.format("%s messages loaded.",
                                    ((TdApi.Messages) Objects.requireNonNull(map.get("result"))).messages.length));
                            loadMessages(
                                    ((TdApi.Messages) Objects.requireNonNull(map.get("result")))
                            );
                            listener.onSuccess(map);
                        }
                        return false;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                }
        );
    }

    private void loadMessages(TdApi.Messages messages) {
        ArrayList<Message> msgs = new ArrayList<>();
        for (TdApi.Message msg: messages.messages) {
            long id = msg.id;
            long chat_id = msg.chatId;
            long author_id = msg.senderId instanceof TdApi.MessageSenderChat ?
                    ((TdApi.MessageSenderChat) msg.senderId).chatId :
                    ((TdApi.MessageSenderUser) msg.senderId).userId;
            String text;
            if(msg.content instanceof TdApi.MessageText) {
                text = ((TdApi.MessageText) msg.content).text.text;
            } else {
                text = "[Unsupported message type]";
            }

            Message message = new Message(id, chat_id, author_id, text, new Date(msg.date), !msg.isOutgoing);
            msgs.add(message);
        }
        Collections.reverse(msgs);
        this.messages = msgs;
    }
}
