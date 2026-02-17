package dev.tinelix.jabwave.api.base;

import android.content.Context;
import android.os.Build;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.tinelix.jabwave.api.base.entities.Authenticator;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.base.models.Chats;

public class BaseClient {

    private final Context ctx;
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

    public static final String API_CLIENT_TAG = "JabwaveAPI";

    protected BaseClient(Context ctx, boolean asyncAPI, String network_name) {
        this.ctx = ctx;
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

    public static class ClientIdentityParams {
        private final String client_name;
        private final String client_version;
        private final String os_version;
        public ClientIdentityParams() {
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

    public Context getServiceContext() {
        return ctx;
    }

    public String getConnectionErrorMessage(String message) {
        int msg_index = message.indexOf("<text");
        if(msg_index >= 0) {
            String regex = "<text (.*)>(.*)</text>";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message.substring(msg_index));
            if(matcher.find() && matcher.groupCount() > 1) {
                return matcher.group(2);
            } else {
                return matcher.group(2);
            }
        } else {
            return "Unknown";
        }
    }
}