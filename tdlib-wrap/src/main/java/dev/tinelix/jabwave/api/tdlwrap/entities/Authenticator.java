package dev.tinelix.jabwave.api.tdlwrap.entities;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;

import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.tdlwrap.TDLibClient;

public class Authenticator extends dev.tinelix.jabwave.api.base.entities.Authenticator
        implements OnClientAPIResultListener {

    private final TDLibClient client;
    private TdApi.PhoneNumberAuthenticationSettings phoneAuthSettings;

    public Authenticator(TDLibClient client) {
        super(client);
        isChangeableAuthData = true;
        authType =
                dev.tinelix.jabwave.api.base.entities.Authenticator.TYPE_REQUIRES_PHONE_NUMBER
                        | dev.tinelix.jabwave.api.base.entities.Authenticator.TYPE_STEP_BY_STEP;
        this.client = client;
        checkAuthState();
    }

    public void checkAuthState() {
        client.send(new TdApi.GetAuthorizationState(), this);
    }

    public void setAuthState(TdApi.AuthorizationState state) {
        if(state.getConstructor() == TdApi.AuthorizationStateReady.CONSTRUCTOR) {
            isAuthenticated = true;
        }
    }

    public void checkPhoneNumber(String phone_number) {
        if(!isAuthenticated) {
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
        if(!isAuthenticated) {
            client.send(
                    new TdApi.CheckAuthenticationPassword(password),
                    this
            );
        }
    }

    public void sendAuthCode(String auth_code) {
        if(!isAuthenticated) {
            client.send(
                    new TdApi.CheckAuthenticationCode(auth_code),
                    this
            );
        }
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
