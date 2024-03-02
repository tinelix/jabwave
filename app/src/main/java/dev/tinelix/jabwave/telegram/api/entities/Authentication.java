package dev.tinelix.jabwave.telegram.api.entities;

import android.annotation.SuppressLint;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.telegram.api.TDLibClient;

public class Authentication implements TDLibClient.ApiHandler {

    private final TDLibClient client;
    private final TDLibClient.ApiHandler handler;
    private boolean isAuthorized;
    private TdApi.PhoneNumberAuthenticationSettings phoneAuthSettings;

    public Authentication(TDLibClient client, TDLibClient.ApiHandler handler) {
        this.handler = handler;
        this.client = client;
    }

    public void checkPhoneNumber(String phone_number) {
        phoneAuthSettings = new TdApi.PhoneNumberAuthenticationSettings();
        phoneAuthSettings.allowFlashCall = false;
        phoneAuthSettings.allowMissedCall = false;
        phoneAuthSettings.allowSmsRetrieverApi = false;
        client.send(
                new TdApi.SetAuthenticationPhoneNumber(phone_number, phoneAuthSettings),
                this
        );
    }

    public void sendCloudPassword(String password) {
        client.send(
                new TdApi.CheckAuthenticationPassword(password),
                this
        );
    }

    public void sendAuthCode(String auth_code) {
        client.send(
                new TdApi.CheckAuthenticationCode(auth_code),
                this
        );
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    @Override
    public void onSuccess(TdApi.Function function, TdApi.Object object) {
        if(object instanceof TdApi.Error) {
            onFail(function, new TDLibClient.Error("auth_error", ((TdApi.Error) object).message));
        } else {
            handler.onSuccess(function, object);
        }
    }

    @Override
    public void onFail(TdApi.Function function, Throwable throwable) {
        throwable.printStackTrace();
        handler.onFail(function, throwable);
    }
}
