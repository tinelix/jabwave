package dev.tinelix.jabwave.api.tdlwrap.entities;

import android.annotation.SuppressLint;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.attachments.Attachment;
import dev.tinelix.jabwave.api.base.entities.Message;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.tdlwrap.attachments.PhotoAttachment;
import dev.tinelix.jabwave.api.tdlwrap.attachments.VideoAttachment;

public class SuperChat extends dev.tinelix.jabwave.api.base.entities.SuperChat {
    public String title;
    public long id;
    public ArrayList<String> groups;

    public SuperChat(String title) {
        super(title, 1, false);
        this.title = title;
    }

    public SuperChat(long id, String title, ArrayList<String> groups, int status) {
        super(id, title, 1, false);
        this.title = title;
        this.id = id;
        this.groups = groups;
        this.status = status;
    }

    public SuperChat(long id, int type, String title, ArrayList<String> groups, int status) {
        super(id, title, 2, 1, false);
        this.title = title;
        this.id = id;
        this.groups = groups;
        this.status = status;
    }

    public SuperChat(long id) {
        super(id, "(Unknown)", 1, false);
        this.id = id;
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
    public Message searchMessageById(long id) {
        for(Message message : messages) {
            if(message.id == id) {
                return message;
            }
        }
        return null;
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
            ArrayList<Attachment> attachments = new ArrayList<>();
            String text = "[Unsupported message type]";
            Attachment attach;
            switch(msg.content.getConstructor()) {
                case TdApi.MessageText.CONSTRUCTOR:
                    text = ((TdApi.MessageText) msg.content).text.text;
                    break;
                case TdApi.MessagePhoto.CONSTRUCTOR:
                    TdApi.MessagePhoto photo = (TdApi.MessagePhoto) msg.content;
                    TdApi.PhotoSize size = photo.photo.sizes[photo.photo.sizes.length - 1];
                    attach = new PhotoAttachment(photo);
                    attachments.add(attach);
                    text = photo.caption.text;
                    break;
                case TdApi.MessageVideo.CONSTRUCTOR:
                    TdApi.MessageVideo video = (TdApi.MessageVideo) msg.content;
                    text = video.caption.text;
                    attach = new VideoAttachment(video.video);
                    attachments.add(attach);
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
                        message.setAttachments(attachments);
                        msgs.add(message);
                        latch.countDown();
                        if(latch.getCount() == 0) {
                            SuperChat.this.messages = msgs;
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
                message.setAttachments(attachments);
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

    @Override
    public int getMessageIndexByFileId(int file_id) {
        for(int i = 0; i < messages.size(); i++) {
            if(messages.get(i).searchAttachment(file_id) != null) {
                return i;
            }
        }
        return -1;
    }
}
