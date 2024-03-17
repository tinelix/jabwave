package dev.tinelix.jabwave.api.base.entities;

import java.util.HashMap;

import dev.tinelix.jabwave.api.base.BaseClient;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;

public class Authenticator {
    protected final BaseClient client;
    protected boolean isAuthenticated;
    protected boolean isChangeableAuthData;
    protected boolean isChangeablePassword;
    protected boolean isChangeableEmail;
    protected boolean isChangeablePhoneNumber;
    protected int authType;
    public static final int TYPE_REQUIRES_EMAIL             =   1;
    public static final int TYPE_REQUIRES_PHONE_NUMBER      =   2;
    public static final int TYPE_STEP_BY_STEP               = 100;

    protected Authenticator(BaseClient client) {
        this.client = client;
    }

    public void signIn(Object object) {
        client.send(object);
    }

    public void signIn(Object object, OnClientAPIResultListener listener) {
        client.send(object, new OnClientAPIResultListener() {
            @Override
            public boolean onSuccess(HashMap<String, Object> map) {
                listener.onSuccess(map);
                return false;
            }

            @Override
            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                listener.onFail(map, t);
                return false;
            }
        });
    }

    protected void setAuthState(boolean value) {
        isAuthenticated = value;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public boolean isChangeableAuthData() {
        return isChangeableAuthData;
    }

    public boolean isChangeablePassword() {
        return isChangeablePassword;
    }

    public boolean isChangeableEmail() {
        return isChangeableEmail;
    }

    public boolean isChangeablePhoneNumber() {
        return isChangeablePhoneNumber;
    }

    public int getType() {
        return authType;
    }
}
