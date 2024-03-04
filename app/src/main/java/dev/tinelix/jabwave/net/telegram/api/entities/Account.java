package dev.tinelix.jabwave.net.telegram.api.entities;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import dev.tinelix.jabwave.net.base.api.listeners.OnClientAPIResultListener;
import dev.tinelix.jabwave.net.telegram.api.TDLibClient;

public class Account extends dev.tinelix.jabwave.net.base.api.entities.Account {
    private TDLibClient client;
    public String username;
    private String phone_number;
    public String about;

    public Account(long id, String first_name, String last_name) {
        super(id, first_name, last_name);
    }

    public Account(TDLibClient client, OnClientAPIResultListener listener) {
        super(client);
        this.client = client;
        client.send(new TdApi.GetMe(), new OnClientAPIResultListener() {
            @Override
            public boolean onSuccess(HashMap<String, Object> map) {
                TdApi.Object object = (TdApi.Object) map.get("result");
                if(object instanceof TdApi.User) {
                    TdApi.User user = ((TdApi.User) object);
                    id = user.id;
                    first_name = user.firstName;
                    last_name = user.lastName;
                    username = user.username;
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
}
