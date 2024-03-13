package dev.tinelix.jabwave.api.base.entities;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.base.listeners.OnClientUpdateListener;

public class SuperChat extends Chat {

    protected boolean requiredAuth;
    protected boolean isJoined;

    public SuperChat(String title, int network_type, boolean requiredAuth) {
        super(title, network_type);
        this.requiredAuth = requiredAuth;
    }

    public SuperChat(Object id, String title, int network_type, boolean requiredAuth) {
        super(id, title, network_type);
        this.requiredAuth = requiredAuth;
    }

    public SuperChat(Object id, String title, int type, int network_type, boolean requiredAuth) {
        super(id, title, type, network_type);
        this.requiredAuth = requiredAuth;
    }

    @Override
    public void loadMessages(BaseClient client) {

    }

    public void join(BaseClient client) {

    }

    public void join(BaseClient client, String nickname) {

    }

    public void join(BaseClient client, String nickname, OnClientUpdateListener listener) {

    }

    public void join(BaseClient client, String nickname, String password_hash) {

    }

    public void join(BaseClient client, String nickname, String password_hash, OnClientUpdateListener listener) {

    }

    @Override
    public void sendMessage(BaseClient client, String text, OnClientAPIResultListener listener) {

    }

    @Override
    public void sendMessage(BaseClient client, String text) {

    }

    public boolean isRequiredAuth() {
        return requiredAuth;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void leave() {
        isJoined = false;
    }
}
