package dev.tinelix.jabwave.net.telegram.api.entities;

import android.annotation.SuppressLint;
import android.util.Log;

import org.drinkless.td.libcore.telegram.Client;
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
import java.util.concurrent.CountDownLatch;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.entities.Message;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;

public class Chat extends dev.tinelix.jabwave.net.base.api.entities.Chat {
    // Contact Class used in Contacts list (AppActivity)
    public String title;
    public long id;
    public ArrayList<String> groups;
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

    public Chat(long id, int type, String title, ArrayList<String> groups, int status) {
        super(id, title, type, 1);
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
                            loadMessages(
                                    client, ((TdApi.Messages) Objects.requireNonNull(map.get("result"))),
                                    listener
                            );
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

    @SuppressLint("SwitchIntDef")
    private void loadMessages(BaseClient client, TdApi.Messages messages,
                              OnClientAPIResultListener listener) {
        ArrayList<Message> msgs = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(messages.totalCount);
        for (TdApi.Message msg: messages.messages) {
            long id = msg.id;
            long chat_id = msg.chatId;
            long author_id = msg.senderId instanceof TdApi.MessageSenderChat ?
                    ((TdApi.MessageSenderChat) msg.senderId).chatId :
                    ((TdApi.MessageSenderUser) msg.senderId).userId;
            String text = "[Unsupported message type]";
            switch(msg.content.getConstructor()) {
                case TdApi.MessageText.CONSTRUCTOR:
                    text = ((TdApi.MessageText) msg.content).text.text;
                    break;
            }

            ChatSender sender = null;
            for (Message msg_entity : msgs) {
                if(msg_entity.getSender() != null) {
                    sender = (ChatSender) msg_entity.getSender();
                }
            }
            if(sender == null && msg.senderId instanceof TdApi.MessageSenderUser) {
                sender = new ChatSender(client, author_id, 0);
                ChatSender finalSender = sender;
                String finalText = text;
                sender.getChatSender(new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        Message message = new Message(id, chat_id, author_id, finalText,
                                new Date(msg.date), !msg.isOutgoing);
                        message.setSender(finalSender);
                        msgs.add(message);
                        latch.countDown();
                        if(latch.getCount() == 0) {
                            Chat.this.messages = msgs;
                            Collections.reverse(msgs);
                            listener.onSuccess(map);
                        }
                        return false;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                });
            } else {
                Message message = new Message(id, chat_id, author_id, text, new Date(msg.date), !msg.isOutgoing);
                msgs.add(message);
                latch.countDown();
                if(latch.getCount() == 0) {
                    this.messages = msgs;
                    Collections.reverse(msgs);
                    listener.onSuccess(new HashMap<>());
                }
            }
        }
    }


    @Override
    public void sendMessage(BaseClient client, String text, OnClientAPIResultListener listener) {
        client.send(
                new TdApi.SendMessage(
                        id, 0, 0, null, null,
                        new TdApi.InputMessageText(
                                new TdApi.FormattedText(text, null),
                                false, true
                        )
                ), new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        if(map.get("result") instanceof TdApi.Message) {
                            if(map.containsKey("result")) {
                                TdApi.Message msg = (TdApi.Message) map.get("result");
                                Message message = new Message(
                                        Objects.requireNonNull(msg).id, msg.chatId, msg.senderId,
                                        text, new Date(msg.date), false
                                );
                                messages.add(message);
                            }
                        }
                        listener.onSuccess(map);
                        return false;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        listener.onFail(map, t);
                        return false;
                    }
                }
        );
    }
}
