package dev.tinelix.jabwave.net.base.api;

import dev.tinelix.jabwave.net.base.api.entities.Authenticator;
import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.base.api.models.Chats;

public class BaseClient {

    /** <b>BaseClient</b> - a dummy mediator class between two or more
     *  structurally incompatible API clients.
     *  <br>
     *  <br>Supports non-async and async API methods and can be used to
     *  develop network-specific functions.
     *   */

    private Authenticator auth;
    protected boolean asyncAPI;
    protected String network_name;
    private Chats chats;

    protected BaseClient(boolean asyncAPI, String network_name) {
        this.asyncAPI = asyncAPI;
        this.network_name = network_name;
    }

    public boolean send(Object object) {
        if(isAsyncApi()) {
            throw new IllegalStateException(
                    "This client can use only asynchronous methods."
            );
        } else {
            return false;
        }
    }

    public void send(Object object, OnClientAPIResultListener listener) {

    }

    public Authenticator getAuthenticator() {
        return auth;
    }

    public Chats getChats() {
        return chats;
    }

    public boolean isAsyncApi() {
        return asyncAPI;
    }
}