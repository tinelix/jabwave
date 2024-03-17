package dev.tinelix.jabwave.api.tdlwrap.entities;

import android.content.Context;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import dev.tinelix.jabwave.api.base.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.api.tdlwrap.TDLibClient;

public class Account extends dev.tinelix.jabwave.api.base.entities.Account {
    private TDLibClient client;
    private boolean isResetPassword;

    public Account(long id, String first_name, String last_name) {
        super(id, first_name, last_name);
        password_type = PASSWORD_TYPE_RESET_AWAIT_7_DAYS;
        password_state = PASSWORD_STATE_ACTIVE;
    }

    public Account(TDLibClient client, OnClientAPIResultListener listener) {
        super(client);
        this.client = client;
        password_type = PASSWORD_TYPE_RESET_AWAIT_7_DAYS;
        password_state = PASSWORD_STATE_ACTIVE;
        client.send(new TdApi.GetMe(), new OnClientAPIResultListener() {
            @Override
            public boolean onSuccess(HashMap<String, Object> map) {
                TdApi.Object object = (TdApi.Object) map.get("result");
                if(object instanceof TdApi.User) {
                    TdApi.User user = ((TdApi.User) object);
                    id = user.id;
                    first_name = user.firstName;
                    last_name = user.lastName;
                    username = "@" + user.username;
                    phone_number = user.phoneNumber;
                    if(user.profilePhoto != null) {
                        downloadPhoto(user.profilePhoto);
                        if(user.profilePhoto.small.local.isDownloadingCompleted)
                            loadPhoto(photo_small, new File(user.profilePhoto.small.local.path));
                        if(user.profilePhoto.big.local.isDownloadingCompleted)
                            loadPhoto(photo, new File(user.profilePhoto.big.local.path));
                    }
                    listener.onSuccess(map);
                }
                return false;
            }

            @Override
            public boolean onFail(HashMap<String, Object> map, Throwable t) {
                return true;
            }
        });
    }

    private void downloadPhoto(TdApi.ProfilePhoto photo) {
        // does not work yet
    }

    private void loadPhoto(byte[] bytes, File file) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(bytes);
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPhoneNumber() {
        return phone_number;
    }

    @Override
    public void resetPassword(String newPassword, OnClientAPIResultListener listener) {
        if(password_type > 0) {
            client.send(new TdApi.ResetPassword(), new OnClientAPIResultListener() {
                @Override
                public boolean onSuccess(HashMap<String, Object> map) {
                    if (map.get("result") instanceof TdApi.ResetPasswordResultOk) {
                        map.put("result", "Done");
                        password_state = 3;
                        listener.onSuccess(map);
                    } else if (map.get("result") instanceof TdApi.ResetPasswordResultPending) {
                        map.put("result", "Pending");
                        password_state = 1;
                        listener.onSuccess(map);
                    } else if (map.get("result") instanceof TdApi.ResetPasswordResultDeclined) {
                        TdApi.ResetPasswordResultDeclined resultDeclined =
                                (TdApi.ResetPasswordResultDeclined) map.get("result");
                        assert resultDeclined != null;
                        map.put("result", String.format("Retry date: %s", resultDeclined.retryDate));
                        map.put("retry_date", resultDeclined.retryDate);
                        password_state = 1;
                        listener.onFail(map, new TDLibClient.Error(
                                "ResetPasswordResultDeclined",
                                (String) map.get("result")));
                    }
                    return false;
                }

                @Override
                public boolean onFail(HashMap<String, Object> map, Throwable t) {
                    return false;
                }
            });
        } else {

        }
    }
}
