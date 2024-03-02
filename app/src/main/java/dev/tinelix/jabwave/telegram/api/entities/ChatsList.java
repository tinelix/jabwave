package dev.tinelix.jabwave.telegram.api.entities;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

import dev.tinelix.jabwave.telegram.api.TDLibClient;

public class ChatsList {

    private final TDLibClient client;
    public ArrayList<Chat> chats;

    public ChatsList(TDLibClient client) {
        this.client = client;
    }

    public void loadChats(TDLibClient.ApiHandler handler) {
        if(chats == null)
            client.send(
                    new TdApi.LoadChats(
                            new TdApi.ChatListMain(), 50
                    ),
                    new TDLibClient.ApiHandler() {
                        @Override
                        public void onSuccess(TdApi.Function function, TdApi.Object object) {
                            chats = new ArrayList<>();
                            if(object instanceof TdApi.Chats) {
                                for (long chatId : ((TdApi.Chats) object).chatIds) {
                                    getChat(chatId, false);
                                }
                            }
                        }

                        @Override
                        public void onFail(TdApi.Function function, Throwable throwable) {

                        }
                    }
            );

    }

    private void getChat(long chatId, boolean clear) {
        if(clear) {
            chats.clear();
        }
        client.send(new TdApi.GetChat(chatId), new TDLibClient.ApiHandler() {
            @Override
            public void onSuccess(TdApi.Function function, TdApi.Object object) {
                chats.add(
                        new Chat(((TdApi.Chat) object).id, ((TdApi.Chat) object).title, new ArrayList<>(), 0)
                );
            }

            @Override
            public void onFail(TdApi.Function function, Throwable throwable) {

            }
        });
    }
}
