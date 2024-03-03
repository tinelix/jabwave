package dev.tinelix.jabwave.telegram.api.entities;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.telegram.api.TDLibClient;

public class ChatsList {

    public final AtomicBoolean processing = new AtomicBoolean(true);

    private final TDLibClient client;
    public ArrayList<dev.tinelix.jabwave.core.ui.list.items.base.Chat> chats;
    private int chats_count;
    private static final int CHATS_MAX_LIMIT = 50;

    public ChatsList(TDLibClient client) {
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
                    dev.tinelix.jabwave.telegram.api.entities.Chat chat =
                            new dev.tinelix.jabwave.telegram.api.entities.Chat(
                                    id, td_chat.title, new ArrayList<>(), 0
                            );
                    ChatsList.this.chats.add(chat);
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
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
