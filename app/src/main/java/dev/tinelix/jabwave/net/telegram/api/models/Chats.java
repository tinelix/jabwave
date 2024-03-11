package dev.tinelix.jabwave.net.telegram.api.models;

import android.annotation.SuppressLint;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.api.base.entities.ChatSender;
import dev.tinelix.jabwave.api.base.entities.Message;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;
import dev.tinelix.jabwave.api.base.entities.Chat;

public class Chats extends dev.tinelix.jabwave.api.base.models.Chats {

    private TDLibClient client;
    public ArrayList<Chat> chats;
    private int chats_count;
    private static final int CHATS_MAX_LIMIT = 80;

    public Chats(TDLibClient client) {
        super(client);
        this.client = client;
        this.chats = new ArrayList<>();
    }

    @Override
    public void loadChats(OnClientAPIResultListener listener) {
        client.send(new TdApi.GetChats(new TdApi.ChatListMain(), CHATS_MAX_LIMIT),
                new OnClientAPIResultListener() {
                    @Override
                    public boolean onSuccess(HashMap<String, Object> map) {
                        TdApi.Object object = (TdApi.Object) map.get("result");
                        if(object instanceof TdApi.Chats) {
                            loadChats((TdApi.Chats) object, listener);
                        }
                        return true;
                    }

                    @Override
                    public boolean onFail(HashMap<String, Object> map, Throwable t) {
                        return false;
                    }
                }
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadChats(TdApi.Chats chats, OnClientAPIResultListener listener) {
        chats_count = chats.totalCount;
        if(chats_count > CHATS_MAX_LIMIT) {
            chats_count = CHATS_MAX_LIMIT;
        }
        this.chats = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(chats_count);
        for (long id: chats.chatIds) {
            client.send(new TdApi.GetChat(id), new OnClientAPIResultListener() {
                @SuppressLint("SwitchIntDef")
                @Override
                public boolean onSuccess(HashMap<String, Object> map) {
                    TdApi.Object object = (TdApi.Object) map.get("result");
                    TdApi.Chat td_chat = (TdApi.Chat) object;
                    int chat_type;
                    switch (Objects.requireNonNull(td_chat).type.getConstructor()) {
                        case TdApi.ChatTypeSupergroup.CONSTRUCTOR:
                            TdApi.ChatTypeSupergroup type =
                                    (TdApi.ChatTypeSupergroup)
                                            Objects.requireNonNull(td_chat).type;
                            if(type.isChannel) {
                                chat_type = 3;
                            } else {
                                chat_type = 2;
                            }
                            break;
                        case TdApi.ChatTypeBasicGroup.CONSTRUCTOR:
                            chat_type = 2;
                            break;
                        case TdApi.ChatTypeSecret.CONSTRUCTOR:
                            chat_type = 1;
                            break;
                        default:
                            chat_type = 0;
                            break;
                    }
                    dev.tinelix.jabwave.net.telegram.api.entities.Chat chat =
                            new dev.tinelix.jabwave.net.telegram.api.entities.Chat(
                                    id, chat_type, td_chat.title, new ArrayList<>(), 0
                            );
                    Chats.this.chats.add(chat);
                    latch.countDown();
                    if(latch.getCount() == 0) {
                        listener.onSuccess(map);
                    }
                    return false;
                }

                @Override
                public boolean onFail(HashMap<String, Object> map, Throwable t) {
                    return false;
                }
            });
        }
        try {
            latch.await(40, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(JabwaveApp.TELEGRAM_SERV_TAG, String.format("%s chats loaded.", this.chats.size()));
    }

    @Override
    public Chat getChatById(Object id) {
        for (Chat chat : chats) {
            if(chat.id.equals(id)) {
                return chat;
            }
        }
        Log.e(JabwaveApp.TELEGRAM_SERV_TAG,
                String.format("Chat not found with ID #%s.", id));
        return null;
    }

    @Override
    public void loadChat(Object chat_id, OnClientAPIResultListener listener) {
        client.send(new TdApi.GetChat((long) chat_id), new OnClientAPIResultListener() {
            @SuppressLint("SwitchIntDef")
            @Override
            public boolean onSuccess(HashMap<String, Object> map) {
                if(map.get("result") instanceof TdApi.Chat) {
                    TdApi.Chat td_chat = (TdApi.Chat) map.get("result");
                    if(td_chat != null) {
                        int chat_type;
                        switch (Objects.requireNonNull(td_chat).type.getConstructor()) {
                            case TdApi.ChatTypeSupergroup.CONSTRUCTOR:
                                TdApi.ChatTypeSupergroup type =
                                        (TdApi.ChatTypeSupergroup)
                                                Objects.requireNonNull(td_chat).type;
                                if(type.isChannel) {
                                    chat_type = 3;
                                } else {
                                    chat_type = 2;
                                }
                                break;
                            case TdApi.ChatTypeBasicGroup.CONSTRUCTOR:
                                chat_type = 2;
                                break;
                            case TdApi.ChatTypeSecret.CONSTRUCTOR:
                                chat_type = 1;
                                break;
                            default:
                                chat_type = 0;
                                break;
                        }
                        long id = td_chat.id;
                        dev.tinelix.jabwave.net.telegram.api.entities.Chat chat =
                                new dev.tinelix.jabwave.net.telegram.api.entities.Chat(
                                        id, chat_type, td_chat.title, new ArrayList<>(), 0
                                );
                        map.put("chat", chat);
                        listener.onSuccess(map);
                    }
                }
                return false;
            }

            @Override
            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                return false;
            }
        });
    }

    public int getTotalCount() {
        return chats_count;
    }

    @Override
    public ArrayList<Chat> getList() {
        return chats;
    }

    @Override
    public int getChatIndex(Chat chat) {
        for(int i = 0; i < chats.size(); i++) {
            if(chats.get(i).equals(chat)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ArrayList<Chat> getChannels() {
        ArrayList<Chat> channels = new ArrayList<>();
        for (Chat chat : chats) {
            if(chat.type == 3) {
                channels.add(chat);
            }
        }
        return channels;
    }

    @Override
    public ArrayList<Chat> getGroupChats() {
        ArrayList<Chat> group_chats = new ArrayList<>();
        for (Chat chat : chats) {
            if(chat.type == 2) {
                group_chats.add(chat);
            }
        }
        return group_chats;
    }

    @Override
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
