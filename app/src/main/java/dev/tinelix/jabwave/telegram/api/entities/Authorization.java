package dev.tinelix.jabwave.telegram.api.entities;

import org.drinkless.td.libcore.telegram.TdApi;

import dev.tinelix.jabwave.telegram.api.TDLibClient;

public class Authorization {

    private final TDLibClient client;
    private boolean isAuthorized;
    private TdApi.PhoneNumberAuthenticationSettings phoneAuthSettings;

    public Authorization(TDLibClient client) {
        this.client = client;
    }

    public void checkPhoneNumber(String phone_number, TDLibClient.ApiHandler handler) {
        phoneAuthSettings = new TdApi.PhoneNumberAuthenticationSettings();
        phoneAuthSettings.allowFlashCall = false;
        phoneAuthSettings.allowMissedCall = false;
        phoneAuthSettings.allowSmsRetrieverApi = false;
        client.send(
                new TdApi.SetAuthenticationPhoneNumber(phone_number, phoneAuthSettings),
                handler
        );
    }

    public void checkAuthenticationPassword(String password, TDLibClient.ApiHandler handler) {
        client.send(
                new TdApi.CheckAuthenticationPassword(password),
                handler
        );
    }

    public void sendAuthCode(String auth_code, TDLibClient.ApiHandler handler) {
        client.send(
                new TdApi.CheckAuthenticationCode(auth_code),
                handler
        );
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }
}
