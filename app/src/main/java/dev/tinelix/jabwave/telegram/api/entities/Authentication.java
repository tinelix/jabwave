package dev.tinelix.jabwave.telegram.api.entities;

import android.annotation.SuppressLint;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.telegram.api.TDLibClient;

public class Authentication implements TDLibClient.ApiHandler {

    private final TDLibClient client;
    private boolean isAuthorized;
    private TdApi.PhoneNumberAuthenticationSettings phoneAuthSettings;

    public Authentication(TDLibClient client) {
        this.client = client;
        checkAuthState();
    }

    public void checkAuthState() {
        client.send(new TdApi.GetAuthorizationState(), this);
    }

    public void setAuthState(TdApi.AuthorizationState state) {
        if(state.getConstructor() == TdApi.AuthorizationStateReady.CONSTRUCTOR) {
            isAuthorized = true;
        }
    }

    public void checkPhoneNumber(String phone_number) {
        if(!isAuthorized) {
            phoneAuthSettings = new TdApi.PhoneNumberAuthenticationSettings();
            phoneAuthSettings.allowFlashCall = false;
            phoneAuthSettings.allowMissedCall = false;
            phoneAuthSettings.allowSmsRetrieverApi = false;
            client.send(
                    new TdApi.SetAuthenticationPhoneNumber(phone_number, phoneAuthSettings),
                    this
            );
        }
    }

    public void sendCloudPassword(String password) {
        if(!isAuthorized) {
            client.send(
                    new TdApi.CheckAuthenticationPassword(password),
                    this
            );
        }
    }

    public void sendAuthCode(String auth_code) {
        if(!isAuthorized) {
            client.send(
                    new TdApi.CheckAuthenticationCode(auth_code),
                    this
            );
        }
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    @Override
    public void onSuccess(TdApi.Function function, TdApi.Object object) {
        if(object instanceof TdApi.Error) {
            onFail(function, new TDLibClient.Error(
                    "auth_error",
                    String.format(
                            "%s\r\nFunction: TdApi.%s",
                            ((TdApi.Error) object).message,
                            function.getClass().getSimpleName()
                    )
            ));
        }
    }

    @Override
    public void onFail(TdApi.Function function, Throwable throwable) {
        throwable.printStackTrace();
        client.apiHandler.onFail(function, throwable);
    }
}
