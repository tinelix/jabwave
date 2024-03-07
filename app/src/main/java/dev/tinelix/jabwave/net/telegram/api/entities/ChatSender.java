package dev.tinelix.jabwave.net.telegram.api.entities;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;
import java.util.Objects;

import dev.tinelix.jabwave.net.base.api.BaseClient;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;

public class ChatSender extends dev.tinelix.jabwave.net.base.api.entities.ChatSender {
    public ChatSender(BaseClient client, Object id, int type) {
        super(client, id, type);
        switch (type) {
            case TdApi.MessageSenderChat.CONSTRUCTOR:
                this.type = 1;
                break;
            default:
                this.type = 0;
                break;
        }
    }

    @Override
    public void getChatSender() {
        if(type == 0) {
            client.send(new TdApi.GetUser((long) id), new OnClientAPIResultListener() {
                @Override
                public boolean onSuccess(HashMap<String, Object> map) {
                    if(map.get("result") instanceof TdApi.User) {
                        TdApi.User user = ((TdApi.User) map.get("result"));
                        first_name = Objects.requireNonNull(user).firstName;
                        last_name = user.lastName;
                    }
                    return true;
                }

                @Override
                public boolean onFail(HashMap<String, Object> map, Throwable t) {
                    return false;
                }
            });
        }
    }

    @Override
    public void getChatSender(OnClientAPIResultListener listener) {
        client.send(new TdApi.GetUser((long) id), new OnClientAPIResultListener() {
            @Override
            public boolean onSuccess(HashMap<String, Object> map) {
                if(map.get("result") instanceof TdApi.User) {
                    TdApi.User user = ((TdApi.User) map.get("result"));
                    first_name = Objects.requireNonNull(user).firstName;
                    last_name = user.lastName;
                    listener.onSuccess(map);
                }
                return true;
            }

            @Override
            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                return false;
            }
        });
    }
}
