package dev.tinelix.jabwave.api.tdlwrap.entities;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;
import java.util.Objects;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

public class ChatSender extends dev.tinelix.jabwave.api.base.entities.ChatSender {
    public ChatSender(BaseClient client, Object id, int type) {
        super(client, id, type);
        this.type = type == TdApi.MessageSenderChat.CONSTRUCTOR ? 1 : 0;
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
