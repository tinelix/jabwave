package dev.tinelix.jabwave.api.base;

import android.content.Context;
import android.os.Build;

import java.util.HashMap;

import dev.tinelix.jabwave.api.base.entities.Authenticator;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.base.models.Chats;

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

    public class ClientIdentityParams {
        private String client_name;
        private String client_version;
        private String os_version;
        public ClientIdentityParams(Context ctx) {
            client_name = "Jabwave";
            client_version = "0.0.1";
            os_version = String.format("Android %s", Build.VERSION.RELEASE);
        }

        public HashMap<String, String> getClientIdentity() {
            HashMap<String, String> map = new HashMap<>();
            map.put("client_name", client_name);
            map.put("client_version", client_version);
            map.put("os_version", os_version);
            return map;
        }
    }
}