package dev.tinelix.jabwave.api.tdlwrap.entities;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;

import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.tdlwrap.TDLibClient;

public class Authenticator extends dev.tinelix.jabwave.api.base.entities.Authenticator
        implements OnClientAPIResultListener {

    private final TDLibClient client;
    private boolean isAuthorized;
    private TdApi.PhoneNumberAuthenticationSettings phoneAuthSettings;

    public Authenticator(TDLibClient client) {
        super(client);
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
    public boolean onSuccess(HashMap<String, Object> map) {
        TdApi.Object object = (TdApi.Object) map.get("result");
        TdApi.Function function = (TdApi.Function) map.get("function");
        if(object instanceof TdApi.Error) {
            onFail(map, new TDLibClient.Error(
                    "auth_error",
                    String.format(
                            "%s\r\nFunction: TdApi.%s",
                            ((TdApi.Error) object).message,
                            function != null ? function.getClass().getSimpleName() : "UnknownClass"
                    )
            ));
        }
        return true;
    }

    @Override
    public boolean onFail(HashMap<String, Object> map, Throwable t) {
        return false;
    }
}
