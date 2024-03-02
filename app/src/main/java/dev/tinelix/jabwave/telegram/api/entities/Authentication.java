package dev.tinelix.jabwave.telegram.api.entities;

import android.annotation.SuppressLint;

import org.drinkless.td.libcore.telegram.TdApi;

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

    public void checkAuthenticationPassword(String password) {
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

    @SuppressLint("SwitchIntDef")
    public void updateState(TDLibClient client, TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                this.onFail(
                        new TDLibClient.Error(
                                TDLibClient.Error.INVALID_TDLIB_PARAMETERS,
                                "Invalid TDLib client parameters"
                        )
                );
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new TDLibClient.ApiHandler() {
                    @Override
                    public void onSuccess(TdApi.Object object) {
                        isAuthorized = true;
                        handler.onSuccess(object);
                    }

                    @Override
                    public void onFail(Throwable throwable) {
                        handler.onFail(throwable);
                    }
                });
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                this.onFail(
                        new TDLibClient.Error(
                                TDLibClient.Error.REQUIRED_PHONE_NUMBER,
                                "Required phone number for authentication."
                        )
                );
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                this.onFail(
                        new TDLibClient.Error(
                                TDLibClient.Error.REQUIRED_AUTH_CODE,
                                "Required authentication code."
                        )
                );
                break;
            default:
                break;
        }
    }

    @Override
    public void onSuccess(TdApi.Object object) {
        if(object instanceof TdApi.Error) {
            onFail(new TDLibClient.Error("auth_error", ((TdApi.Error) object).message));
        } else {
            updateState(this.client, object);
            handler.onSuccess(object);
        }
    }

    @Override
    public void onFail(Throwable throwable) {
        throwable.printStackTrace();
        handler.onFail(throwable);
    }
}
