package dev.tinelix.jabwave.net.telegram.api.models;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;
import dev.tinelix.jabwave.ui.list.items.base.Chat;

public class Chats {

    private final TDLibClient client;
    public ArrayList<Chat> chats;
    private int chats_count;
    private static final int CHATS_MAX_LIMIT = 50;

    public Chats(TDLibClient client) {
        this.client = client;
        this.chats = new ArrayList<>();
    }

    public void loadChats(TDLibClient.ApiHandler handler) {
        client.send(new TdApi.GetChats(new TdApi.ChatListMain(), CHATS_MAX_LIMIT),
                new TDLibClient.ApiHandler() {
                    @Override
                    public void onSuccess(TdApi.Function function, TdApi.Object object) {
                        if(object instanceof TdApi.Chats) {
                            loadChats((TdApi.Chats) object, handler);
                        }
                    }

                    @Override
                    public void onFail(TdApi.Function function, Throwable throwable) {

                    }
                }
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadChats(TdApi.Chats chats, TDLibClient.ApiHandler handler) {
        chats_count = chats.totalCount;
        if(chats_count > CHATS_MAX_LIMIT) {
            chats_count = CHATS_MAX_LIMIT;
        }
        this.chats = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(chats_count);
        for (long id: chats.chatIds) {
            client.send(new TdApi.GetChat(id), new TDLibClient.ApiHandler() {
                @Override
                public void onSuccess(TdApi.Function function, TdApi.Object object) {
                    TdApi.Chat td_chat = (TdApi.Chat) object;
                    dev.tinelix.jabwave.net.telegram.api.entities.Chat chat =
                            new dev.tinelix.jabwave.net.telegram.api.entities.Chat(
                                    id, td_chat.title, new ArrayList<>(), 0
                            );
                    Chats.this.chats.add(chat);
                    latch.countDown();
                    if(latch.getCount() == 0) {
                        handler.onSuccess(function, object);
                    }
                }

                @Override
                public void onFail(TdApi.Function function, Throwable throwable) {

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

    private void getChat(long chatId, boolean clear) {
        if(clear) {
            chats.clear();
        }
        client.send(new TdApi.GetChat(chatId));
    }

    public int getTotalCount() {
        return chats_count;
    }
}
