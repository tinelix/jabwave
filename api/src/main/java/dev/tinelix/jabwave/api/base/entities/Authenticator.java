package dev.tinelix.jabwave.api.base.entities;

import dev.tinelix.jabwave.api.base.BaseClient;

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
