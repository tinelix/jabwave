package dev.tinelix.jabwave.net.base.api.entities;

import dev.tinelix.jabwave.net.base.api.BaseClient;

public class Authenticator {
    private final BaseClient client;
    private boolean isAuthenticated;

    protected Authenticator(BaseClient client) {
        this.client = client;
    }

    public void signIn(Object object) {
        client.send(object);
    }

    protected void setAuthState(boolean value) {
        isAuthenticated = value;
    }
}
